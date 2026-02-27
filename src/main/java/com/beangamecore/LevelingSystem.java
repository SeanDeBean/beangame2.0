package com.beangamecore;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.beangamecore.data.DatabaseManager;
import com.beangamecore.util.Cooldowns;
import com.beangamecore.util.PotionCategories;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LevelingSystem {
    
    private final Map<UUID, PlayerData> playerData = new HashMap<>();
    private final Map<UUID, String> playerTeamNames = new HashMap<>();
    private final Scoreboard scoreboard;
    
    // XP rewards
    private static final int XP_WIN = 10;
    private static final int XP_LOSS = 2;
    private static final int XP_REVIVE = 5;
    
    // Formula: 5 * (1.15 ^ level)
    private static final double BASE_XP = 5.0;
    private static final double MULTIPLIER = 1.15;
    
    public LevelingSystem() {
        this.scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        createTable();
        cleanupOldTeams();
        
        // Start per-tick team update task
        Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), this::updateAllTeams, 0L, 1L);
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
                    Main.logger().info("[LevelingSystem] Updated username for " + currentName + " (was: " + storedName + ")");
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
    
    public void savePlayer(Player player) {
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
        
        playerData.remove(uuid);
        savePlayer(player);
    }
    
    private void createOrUpdatePlayerTeam(Player player, int level) {
        UUID uuid = player.getUniqueId();
        String oldTeamName = playerTeamNames.get(uuid);
        String newTeamName = "playerlevel_" + uuid.toString().substring(0, 8) + "_" + level;
        
        // If already on correct team, still update suffix in case debuffs changed
        if (oldTeamName != null && oldTeamName.equals(newTeamName)) {
            Team team = scoreboard.getTeam(newTeamName);
            if (team != null) {
                String suffix = buildDebuffSuffix(uuid);
                team.setSuffix(suffix);
            }
            return;
        }
        
        // Remove from old team and delete if empty
        if (oldTeamName != null) {
            Team oldTeam = scoreboard.getTeam(oldTeamName);
            if (oldTeam != null) {
                oldTeam.removeEntry(player.getName());
                if (oldTeam.getEntries().isEmpty()) {
                    oldTeam.unregister();
                }
            }
        }
        
        // Create or get new team
        Team newTeam = scoreboard.getTeam(newTeamName);
        if (newTeam == null) {
            newTeam = scoreboard.registerNewTeam(newTeamName);
        }
        
        // Set prefix with level and color
        String color = getColorForLevel(level);
        newTeam.setPrefix(color + "[" + level + "] " + ChatColor.RESET);
        
        // Set suffix with debuff indicators
        String suffix = buildDebuffSuffix(uuid);
        if (!suffix.isEmpty()) {
            newTeam.setSuffix(suffix);
        } else {
            newTeam.setSuffix(""); // Clear suffix if no debuffs
        }
        
        // Add player to team
        newTeam.addEntry(player.getName());
        playerTeamNames.put(uuid, newTeamName);
    }

    private String buildDebuffSuffix(UUID uuid) {
        StringBuilder suffix = new StringBuilder();
        
        Map<String, String> debuffEmojis = Map.of(
            "attack", "☠",
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
                String emoji = debuffEmojis.getOrDefault(debuff, "?");
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
    
    public void addXp(Player player, int amount) {
        UUID uuid = player.getUniqueId();
        PlayerData data = playerData.get(uuid);
        
        // If not in memory, load from database first
        if (data == null) {
            data = loadPlayerDataFromDatabase(player);
            if (data == null) {
                data = new PlayerData(); // New player
            }
            playerData.put(uuid, data);
            createOrUpdatePlayerTeam(player, data.getLevel());
        }
        
        int oldLevel = data.getLevel();
        data.addXp(amount);
        
        while (data.getXp() >= getXpNeededForLevel(data.getLevel())) {
            data.levelUp();
        }
        
        if (data.getLevel() != oldLevel) {
            createOrUpdatePlayerTeam(player, data.getLevel());
        }
        
        savePlayer(player);
    }

    private PlayerData loadPlayerDataFromDatabase(Player player) {
        String sql = "SELECT level, xp FROM players WHERE uuid = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, player.getUniqueId().toString());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                PlayerData data = new PlayerData();
                data.setLevel(rs.getInt("level"));
                data.setXp(rs.getInt("xp"));
                return data;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public void onWin(Player player) {
        addXp(player, XP_WIN);
    }
    
    public void onLoss(Player player) {
        addXp(player, XP_LOSS);
    }
    
    public void onRevive(Player player) {
        addXp(player, XP_REVIVE);
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