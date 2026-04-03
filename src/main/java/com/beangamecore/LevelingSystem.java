package com.beangamecore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.beangamecore.commands.BeangameStart;
import com.beangamecore.data.DatabaseManager;
import com.beangamecore.util.Cooldowns;
import com.beangamecore.util.PotionCategories;

public class LevelingSystem {
    
    private final Map<UUID, PlayerData> playerData = new HashMap<>();
    private final Map<UUID, String> playerTeamNames = new HashMap<>();
    private final Scoreboard scoreboard;
    
    // XP rewards
    private static final int XP_WIN = 10;
    private static final int XP_LOSS = 4;
    private static final int XP_ROLL = 2;
    private static final int XP_REVIVE = 5;
    
    // Formula: 5 * (1.15 ^ level)
    private static final double BASE_XP = 5.0;
    private static final double MULTIPLIER = 1.15;
    
    // Queue for XP processing
    private static final Queue<XpUpdateRequest> xpUpdateQueue = new ConcurrentLinkedQueue<>();
    private static final int XP_BATCH_SIZE = 10;
    private static final long XP_DELAY_BETWEEN_BATCHES = 1L; // 1 tick between batches
    private static boolean isProcessingXp = false;
    
    // Queue for database saves (separate from XP processing to save async)
    private static final Queue<Player> saveQueue = new ConcurrentLinkedQueue<>();
    private static final int SAVE_BATCH_SIZE = 5;
    private static final long SAVE_DELAY_BETWEEN_BATCHES = 5L; // 5 ticks between save batches
    private static boolean isProcessingSaves = false;

    public LevelingSystem() {
        this.scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        createTable();
        cleanupOldTeams();
        
        // Start per-tick team update task
        Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), this::updateAllTeams, 0L, 1L);
    }
    
    // Inner class to hold XP update requests
    private static class XpUpdateRequest {
        final UUID playerUuid;
        final String playerName;
        final int amount;
        final XpType type;
        
        XpUpdateRequest(UUID playerUuid, String playerName, int amount, XpType type) {
            this.playerUuid = playerUuid;
            this.playerName = playerName;
            this.amount = amount;
            this.type = type;
        }
    }
    
    private enum XpType {
        WIN, LOSS, ROLL, REVIVE, CUSTOM
    }
    
    private void createTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS players (
                uuid VARCHAR(36) PRIMARY KEY,
                player_name VARCHAR(16) NOT NULL,
                level INT DEFAULT 1,
                xp INT DEFAULT 0,
                last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
            """;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.execute();
            Main.logger().info("[LevelingSystem] Players table ready.");
        } catch (SQLException e) {
            e.printStackTrace();
            Main.logger().severe("[LevelingSystem] Failed to create players table!");
        }
    }
    
    private void cleanupOldTeams() {
        // Remove any existing level teams from previous sessions
        for (Team team : scoreboard.getTeams()) {
            if (team.getName().startsWith("lvl_") || team.getName().startsWith("playerlevel_")) {
                team.unregister();
            }
        }
    }
    
    private String getColorForLevel(int level) {
        int tier = level / 5;
        return switch (tier) {
            case 0 -> "§f";
            case 1 -> "§2";
            case 2 -> "§a";
            case 3 -> "§b";
            case 4 -> "§3";
            case 5 -> "§9";
            case 6 -> "§d";
            case 7 -> "§5";
            case 8 -> "§4";
            case 9 -> "§c";
            default -> "§6";
        };
    }
    
    public int getXpNeededForLevel(int level) {
        return (int) Math.ceil(BASE_XP * Math.pow(MULTIPLIER, level));
    }
    
    public void loadPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        String currentName = player.getName();
        String sql = "SELECT player_name, level, xp FROM players WHERE uuid = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            
            PlayerData data = new PlayerData();
            
            if (rs.next()) {
                String storedName = rs.getString("player_name");
                data.setLevel(rs.getInt("level"));
                data.setXp(rs.getInt("xp"));
                
                if (!storedName.equals(currentName)) {
                    updatePlayerName(uuid, currentName);
                    Main.logger().info(() -> "[LevelingSystem] Updated username for " + currentName + " (was: " + storedName + ")");
                }
            } else {
                insertNewPlayer(player);
            }
            
            playerData.put(uuid, data);
            createOrUpdatePlayerTeam(player, data.getLevel());
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void insertNewPlayer(Player player) {
        String sql = "INSERT INTO players (uuid, player_name, level, xp) VALUES (?, ?, 1, 0)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, player.getUniqueId().toString());
            stmt.setString(2, player.getName());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void updatePlayerName(UUID uuid, String newName) {
        String sql = "UPDATE players SET player_name = ? WHERE uuid = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newName);
            stmt.setString(2, uuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // Modified: Queue save instead of immediate execution
    public void savePlayer(Player player) {
        if (!saveQueue.contains(player)) {
            saveQueue.add(player);
        }
        startSaveProcessing();
    }
    
    // New: Start save processing if not already running
    private void startSaveProcessing() {
        if (isProcessingSaves) {
            return;
        }
        isProcessingSaves = true;
        processSaveBatch();
    }
    
    // New: Process save queue in batches asynchronously
    private void processSaveBatch() {
        if (saveQueue.isEmpty()) {
            isProcessingSaves = false;
            return;
        }
        
        Bukkit.getScheduler().runTaskAsynchronously(Main.getPlugin(), () -> {
            List<Player> batch = new ArrayList<>();
            while (!saveQueue.isEmpty() && batch.size() < SAVE_BATCH_SIZE) {
                Player player = saveQueue.poll();
                if (player != null) {
                    batch.add(player);
                }
            }
            
            if (!batch.isEmpty()) {
                performBatchSave(batch);
            }
            
            // Schedule next batch on main thread
            Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
                if (!saveQueue.isEmpty()) {
                    processSaveBatch();
                } else {
                    isProcessingSaves = false;
                }
            }, SAVE_DELAY_BETWEEN_BATCHES);
        });
    }
    
    // New: Perform actual batch database save
    private void performBatchSave(List<Player> players) {
        String sql = "UPDATE players SET player_name = ?, level = ?, xp = ? WHERE uuid = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            for (Player player : players) {
                PlayerData data = playerData.get(player.getUniqueId());
                if (data == null) continue;
                
                stmt.setString(1, player.getName());
                stmt.setInt(2, data.getLevel());
                stmt.setInt(3, data.getXp());
                stmt.setString(4, player.getUniqueId().toString());
                stmt.addBatch();
            }
            
            stmt.executeBatch();
            
        } catch (SQLException e) {
            Main.getPlugin().getLogger().severe(() -> "[LevelingSystem] Failed to save batch: " + e.getMessage());
            // Re-add failed players to queue
            for (Player player : players) {
                if (!saveQueue.contains(player)) {
                    saveQueue.add(player);
                }
            }
        }
    }
    
    public void unloadPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        
        // Remove from team and delete the team
        String teamName = playerTeamNames.remove(uuid);
        if (teamName != null) {
            Team team = scoreboard.getTeam(teamName);
            if (team != null) {
                team.removeEntry(player.getName());
                if (team.getEntries().isEmpty()) {
                    team.unregister();
                }
            }
        }
        
        // Immediate save on unload to ensure data persistence
        performImmediateSave(player);
        playerData.remove(uuid);
    }
    
    // New: Immediate save for critical operations (unload, etc.)
    private void performImmediateSave(Player player) {
        PlayerData data = playerData.get(player.getUniqueId());
        if (data == null) return;
        
        String sql = "UPDATE players SET player_name = ?, level = ?, xp = ? WHERE uuid = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, player.getName());
            stmt.setInt(2, data.getLevel());
            stmt.setInt(3, data.getXp());
            stmt.setString(4, player.getUniqueId().toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createOrUpdatePlayerTeam(Player player, int level) {
        UUID uuid = player.getUniqueId();
        // Use consistent alphabetical ordering by player name
        String newTeamName = "level_" + player.getName().toLowerCase();
        
        // If already on correct team, just update prefix/suffix
        if (playerTeamNames.containsKey(uuid) && 
            scoreboard.getTeam(playerTeamNames.get(uuid)) != null &&
            playerTeamNames.get(uuid).equals(newTeamName)) {
            
            Team team = scoreboard.getTeam(newTeamName);
            String prefix = getColorForLevel(level) + "[" + level + "] " + ChatColor.RESET;
            team.setPrefix(prefix);
            team.setSuffix(buildDebuffSuffix(uuid));
            return;
        }
        
        // Remove from old team
        String oldTeamName = playerTeamNames.remove(uuid);
        if (oldTeamName != null) {
            Team oldTeam = scoreboard.getTeam(oldTeamName);
            if (oldTeam != null) {
                oldTeam.removeEntry(player.getName());
                if (oldTeam.getEntries().isEmpty()) {
                    oldTeam.unregister();
                }
            }
        }
        
        // Create or get team
        Team newTeam = scoreboard.getTeam(newTeamName);
        if (newTeam == null) {
            newTeam = scoreboard.registerNewTeam(newTeamName);
        }
        
        // Set prefix with level and color
        String color = getColorForLevel(level);
        newTeam.setPrefix(color + "[" + level + "] " + ChatColor.RESET);
        newTeam.setSuffix(buildDebuffSuffix(uuid));
        
        // Add player to team
        newTeam.addEntry(player.getName());
        playerTeamNames.put(uuid, newTeamName);
    }

    private String buildDebuffSuffix(UUID uuid) {
        StringBuilder suffix = new StringBuilder();
        
        Map<String, String> debuffEmojis = Map.of(
            "attack", "§f☠",
            "use_item", "§c✖",
            "slot_enforced", "§e✎",
            "immobilized", "§b❄",
            "silenced", "§0⌀",
            "schizophrenic", "§d♪",
            "jumbling", "§5☢",
            "redacted", "§8█"
        );
        
        for (String debuff : PotionCategories.getHarmfulCustomPotions()) {
            if (Cooldowns.onCooldown(debuff, uuid)) {
                String emoji = debuffEmojis.entrySet().stream()
                    .filter(e -> e.getKey().equalsIgnoreCase(debuff))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse("?");
                suffix.append(emoji);
            }
        }
        
        // Only add leading space if there are actual debuffs
        if (suffix.length() > 0) {
            return " " + suffix.toString();
        }
        
        return ""; // Return empty string if no debuffs
    }
    
    private void updateAllTeams() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData data = playerData.get(player.getUniqueId());
            if (data != null) {
                createOrUpdatePlayerTeam(player, data.getLevel());
            }
        }
    }
    
    // Modified: Queue XP update instead of immediate processing
    public void addXp(Player player, int amount) {
        UUID uuid = player.getUniqueId();
        
        // Ensure player data is loaded
        if (!playerData.containsKey(uuid)) {
            loadPlayer(player);
        }
        
        // Add to processing queue
        xpUpdateQueue.add(new XpUpdateRequest(uuid, player.getName(), amount, XpType.CUSTOM));
        startXpProcessing();
    }
    
    // New: Start XP processing if not already running
    private void startXpProcessing() {
        if (isProcessingXp) {
            return;
        }
        isProcessingXp = true;
        processXpBatch();
    }
    
    // New: Process XP queue in batches on the main thread (sync for thread safety with HashMap)
    private void processXpBatch() {
        if (xpUpdateQueue.isEmpty()) {
            isProcessingXp = false;
            return;
        }
        
        Bukkit.getScheduler().runTask(Main.getPlugin(), () -> {
            int processed = 0;
            Set<UUID> playersToSave = new HashSet<>();
            
            while (!xpUpdateQueue.isEmpty() && processed < XP_BATCH_SIZE) {
                XpUpdateRequest request = xpUpdateQueue.poll();
                if (request == null) continue;
                
                PlayerData data = playerData.get(request.playerUuid);
                if (data == null) continue;
                
                int oldLevel = data.getLevel();
                data.addXp(request.amount);
                
                // Check for level up
                while (data.getXp() >= getXpNeededForLevel(data.getLevel())) {
                    data.levelUp();
                }
                
                // Update team if level changed
                if (data.getLevel() != oldLevel) {
                    Player player = Bukkit.getPlayer(request.playerUuid);
                    if (player != null && player.isOnline()) {
                        createOrUpdatePlayerTeam(player, data.getLevel());
                    }
                }
                
                playersToSave.add(request.playerUuid);
                processed++;
            }
            
            // Queue players for async save
            for (UUID uuid : playersToSave) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null && player.isOnline()) {
                    savePlayer(player);
                }
            }
            
            // Schedule next batch
            if (!xpUpdateQueue.isEmpty()) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), 
                    this::processXpBatch, XP_DELAY_BETWEEN_BATCHES);
            } else {
                isProcessingXp = false;
            }
        });
    }
    
    // Modified event methods to use queue
    public void onWin(Player player) {
        queueXpEvent(player, XP_WIN, XpType.WIN);
    }
    
    public void onLoss(Player player) {
        queueXpEvent(player, XP_LOSS, XpType.LOSS);
    }
    
    public void onRevive(Player player) {
        queueXpEvent(player, XP_REVIVE, XpType.REVIVE);
    }

    public void onRoll(Player player) {
        if(BeangameStart.getAutorollRunCount() <= 8) queueXpEvent(player, XP_ROLL, XpType.ROLL);
    }
    
    // New: Helper to queue XP events
    private void queueXpEvent(Player player, int amount, XpType type) {
        UUID uuid = player.getUniqueId();
        
        // Ensure player data is loaded
        if (!playerData.containsKey(uuid)) {
            loadPlayer(player);
        }
        
        xpUpdateQueue.add(new XpUpdateRequest(uuid, player.getName(), amount, type));
        startXpProcessing();
    }
    
    public int getPlayerLevel(Player player) {
        return playerData.getOrDefault(player.getUniqueId(), new PlayerData()).getLevel();
    }
    
    public int getPlayerXp(Player player) {
        return playerData.getOrDefault(player.getUniqueId(), new PlayerData()).getXp();
    }
    
    public int getXpToNextLevel(Player player) {
        int level = getPlayerLevel(player);
        int currentXp = getPlayerXp(player);
        return getXpNeededForLevel(level) - currentXp;
    }
    
    // Method to add custom prefix/suffix to player's team (for other plugins to use)
    public void setTeamPrefix(Player player, String prefix) {
        String teamName = playerTeamNames.get(player.getUniqueId());
        if (teamName != null) {
            Team team = scoreboard.getTeam(teamName);
            if (team != null) {
                team.setPrefix(prefix);
            }
        }
    }
    
    public void setTeamSuffix(Player player, String suffix) {
        String teamName = playerTeamNames.get(player.getUniqueId());
        if (teamName != null) {
            Team team = scoreboard.getTeam(teamName);
            if (team != null) {
                team.setSuffix(suffix);
            }
        }
    }
    
    public String getTeamName(Player player) {
        return playerTeamNames.get(player.getUniqueId());
    }
    
    // New: Force flush all pending operations (useful on shutdown)
    public void flushAll() {
        // Process all remaining XP updates immediately
        while (!xpUpdateQueue.isEmpty()) {
            XpUpdateRequest request = xpUpdateQueue.poll();
            if (request == null) continue;
            
            PlayerData data = playerData.get(request.playerUuid);
            if (data == null) continue;
            
            data.addXp(request.amount);
            while (data.getXp() >= getXpNeededForLevel(data.getLevel())) {
                data.levelUp();
            }
        }
        
        // Process all remaining saves immediately
        while (!saveQueue.isEmpty()) {
            Player player = saveQueue.poll();
            if (player != null) {
                performImmediateSave(player);
            }
        }
    }
    
    private static class PlayerData {
        private int level = 1;
        private int xp = 0;
        
        public int getLevel() { return level; }
        public void setLevel(int level) { this.level = level; }
        
        public int getXp() { return xp; }
        public void setXp(int xp) { this.xp = xp; }
        
        public void addXp(int amount) {
            this.xp += amount;
        }
        
        public void levelUp() {
            xp -= getXpNeededForLevel(level);
            level++;
        }
        
        private int getXpNeededForLevel(int lvl) {
            return (int) Math.ceil(5.0 * Math.pow(1.15, lvl));
        }
    }
}