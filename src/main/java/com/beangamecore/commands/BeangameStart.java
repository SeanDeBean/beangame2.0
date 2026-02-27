package com.beangamecore.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.WorldType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.beangamecore.LevelingSystem;
import com.beangamecore.Main;
import com.beangamecore.data.DatabaseManager;
import com.beangamecore.items.BorderManipulator;
import com.beangamecore.items.Revive;
import com.beangamecore.items.UltimateGamble;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.registry.BeangameItemRegistry;
import com.beangamecore.util.ItemNBT;
import com.beangamecore.util.ResetItems;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class BeangameStart implements CommandExecutor{
    
    public static boolean startphase = false;
    public static boolean gamerunning = false;
    public static double percent = 0.0;

    public static boolean autoroll = true;

    private static int autorollTaskId = -1;
    private static int autorollRunCount = 0;

    public static int getAutorollRunCount() {
        return autorollRunCount;
    }

    private void startAutorollTask() {
        autoroll = true;
        
        autorollTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), new Runnable() {
            public void run() {
                if (!gamerunning || !autoroll) {
                    stopAutorollTask();
                    return;
                }
                
                // Execute autorole logic here
                executeAutorollLogic();
                
                autorollRunCount++;
                
                // Reschedule with increasing interval
                if (autorollRunCount % 2 == 0) { // Increase interval every 2 runs
                    stopAutorollTask();
                    startAutorollTask(); // Restart with new timing
                }
            }
        }, getAutoroleInterval(), getAutoroleInterval());
        
        taskIds.add(autorollTaskId);
    }

    private long getAutoroleInterval() {
        // Define the schedule: first roll at 1 minute, then increasing intervals
        // The index represents which run we're SCHEDULING for
        int[] minuteSchedule = {1, 1, 1, 2, 2, 3, 3, 4, 4, 5};
        int[] minuteRange = {0, 1, 1, 1, 1, 1, 2, 2, 4, 5};
        
        // We're scheduling the NEXT run, so use current run count as index
        if (autorollRunCount < minuteSchedule.length) {
            int baseMinute = minuteSchedule[autorollRunCount];
            int randomRange = minuteRange[autorollRunCount];
            // Cast to int to ensure proper addition
            int randomMinutes = baseMinute + (int)(Math.random() * (randomRange + 1));
            // Bukkit.getLogger().info("Scheduling autoroll #" + (autorollRunCount + 1) + " in " + randomMinutes + " minutes");
            return randomMinutes * 60 * 20L;
        } else {
            // Bukkit.getLogger().info("Scheduling autoroll in 5 minutes (default interval)");
            return 5 * 60 * 20L;
        }
    }

    private void executeAutorollLogic() {
        Bukkit.getLogger().info("Autorole task executed (run count: " + (autorollRunCount + 1) + ")");
        
        // Use run count + 1 to determine which command to use (since we haven't incremented yet)
        String command = (autorollRunCount == 0) ? "bgdistributefood" : "bgdistribute";
        
        // Rest of your executeAutorollLogic method remains the same...
        // First priority: Player "SeanDeBean" if online and opped
        Player seanDeBean = Bukkit.getPlayer("SeanDeBean");
        if (seanDeBean != null && seanDeBean.isOnline() && seanDeBean.isOp()) {
            seanDeBean.performCommand(command);
            Bukkit.getLogger().info("SeanDeBean executed /" + command + " via autorole");
            return;
        }
        
        // Second priority: Any online operator
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isOp()) {
                player.performCommand(command);
                Bukkit.getLogger().info(player.getName() + " executed /" + command + " via autorole");
                return;
            }
        }
        
        // Third priority: Temporarily op an online player to run the command
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (alivePlayers.contains(player.getUniqueId())) {
                try {
                    player.setOp(true);
                    player.performCommand(command);
                    player.setOp(false);
                    Bukkit.getLogger().info(player.getName() + " temporarily opped to execute /" + command + " via autorole");
                    return;
                } catch (Exception e) {
                    player.setOp(false);
                    Bukkit.getLogger().warning("Failed to execute /" + command + " via " + player.getName());
                }
            }
        }
        
        Bukkit.getLogger().warning("No suitable player found to execute /" + command + " via autorole");
    }

    private void stopAutorollTask() {
        if (autorollTaskId != -1) {
            Bukkit.getScheduler().cancelTask(autorollTaskId);
            taskIds.remove(autorollTaskId);
            autorollTaskId = -1;
        }
    }


    private MultiverseCore multiverseCore;
    private World lobbyWorld;

    public static List<UUID> alivePlayers = new ArrayList<>();

    private Set<Integer> taskIds = new HashSet<>(); // Store task IDs

    private void createBeangameWorld(){
        MultiverseWorld mvLobbyWorld = multiverseCore.getMVWorldManager().getMVWorld("lobby");
        if (mvLobbyWorld != null) {
            lobbyWorld = mvLobbyWorld.getCBWorld(); // Get the CraftBukkit World object
        } else {
            Bukkit.getLogger().severe("Lobby world not found in Multiverse-Core! Disabling plugin.");
            Bukkit.getPluginManager().disablePlugin(Main.getPlugin());
        }

        multiverseCore.getMVWorldManager().addWorld("beangame-world", World.Environment.NORMAL, null, WorldType.NORMAL, true, null);
        MultiverseWorld mvWorld = multiverseCore.getMVWorldManager().getMVWorld("beangame-world");
        
        if (mvWorld == null) {
            Bukkit.getLogger().severe("Failed to create or retrieve the world 'beangame-world'!");
            return;
        }

        World vanillaWorld = mvWorld.getCBWorld();
        Location loc = vanillaWorld.getSpawnLocation();

        // Configure world settings
        setupWorldProperties(vanillaWorld, loc);
        
        // Start async chunk preloading
        preloadSpawnArea(vanillaWorld, 16);
    }

    private final Queue<ChunkCoord> chunkQueue = new ConcurrentLinkedQueue<>();
    private int preloadTaskId = -1;
    private final int CHUNKS_PER_TICK = 1;

    private void preloadSpawnArea(World world, int radius) {
        Location center = world.getSpawnLocation();
        int centerX = center.getBlockX() >> 4;
        int centerZ = center.getBlockZ() >> 4;
        
        // Generate spiral pattern for more natural loading
        for (int r = 0; r <= radius; r++) {
            for (int x = -r; x <= r; x++) {
                for (int z = -r; z <= r; z++) {
                    if (Math.abs(x) == r || Math.abs(z) == r) {
                        chunkQueue.add(new ChunkCoord(world, centerX + x, centerZ + z));
                    }
                }
            }
        }
        
        // Start processing if not already running
        if (preloadTaskId == -1) {
            preloadTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                Main.getPlugin(),
                this::processChunkQueue,
                1L, 1L); // Process 1 chunk per tick
        }
    }

    private void processChunkQueue() {
        if (chunkQueue.isEmpty()) {
            if (preloadTaskId != -1) {
                Bukkit.getScheduler().cancelTask(preloadTaskId);
                preloadTaskId = -1;
                Bukkit.getLogger().info("Finished preloading spawn chunks!");
                for(Player op : Bukkit.getOnlinePlayers().stream().filter(OfflinePlayer::isOp).toList()){
                    op.sendMessage(ChatColor.GRAY + ChatColor.ITALIC.toString() + "[Finished preloading spawn chunks!]");
                }
            }
            return;
        }

        for (int i = 0; i < CHUNKS_PER_TICK && !chunkQueue.isEmpty(); i++) {
            ChunkCoord coord = chunkQueue.poll();
            if (coord != null) {
                if (!coord.world.isChunkLoaded(coord.x, coord.z)) {
                    // Loads chunk if not already loaded (sync call)
                    Chunk chunk = coord.world.getChunkAt(coord.x, coord.z);

                    // Force it to stay loaded temporarily
                    chunk.setForceLoaded(true);

                    // Un-force load after 10 minutes
                    Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                        if (chunk.isLoaded()) {
                            chunk.setForceLoaded(false);
                        }
                    }, 20L * 60 * 10);
                }
            }
        }

        if (chunkQueue.size() % 20 == 0) {
            Bukkit.getLogger().info("Preloading chunks... " + chunkQueue.size() + " remaining");
            for(Player op : Bukkit.getOnlinePlayers().stream().filter(OfflinePlayer::isOp).toList()){
                op.sendMessage(ChatColor.GRAY + ChatColor.ITALIC.toString() + "[Preloading chunks... " + chunkQueue.size() + " remaining]");
            }
        }
    }

    private static class ChunkCoord {
        final World world;
        final int x;
        final int z;
        
        public ChunkCoord(World world, int x, int z) {
            this.world = world;
            this.x = x;
            this.z = z;
        }
    }

    @SuppressWarnings("unchecked")
    private void setupWorldProperties(World world, Location spawn) {
        spawn.setX(Math.floor(spawn.getX()) + 0.5);
        spawn.setY(Math.floor(spawn.getY()));
        spawn.setZ(Math.floor(spawn.getZ()) + 0.5);
        world.setSpawnLocation(spawn);
        
        WorldBorder border = world.getWorldBorder();
        border.setCenter(spawn);
        border.setSize(501);
        border.setDamageAmount(1);
        border.setDamageBuffer(0.25);
        border.setWarningDistance(5);
        
        world.setTime(0);
        world.setDifficulty(Difficulty.HARD);
        
        // Set game rules
        @SuppressWarnings("rawtypes")
        GameRule[] rules = {
            GameRule.ANNOUNCE_ADVANCEMENTS, GameRule.DO_DAYLIGHT_CYCLE,
            GameRule.DO_LIMITED_CRAFTING, GameRule.DO_WEATHER_CYCLE,
            GameRule.KEEP_INVENTORY, GameRule.LAVA_SOURCE_CONVERSION,
            GameRule.RANDOM_TICK_SPEED, GameRule.SPAWN_RADIUS,
            GameRule.SPECTATORS_GENERATE_CHUNKS, GameRule.WATER_SOURCE_CONVERSION,
            GameRule.DO_IMMEDIATE_RESPAWN, GameRule.SHOW_DEATH_MESSAGES
        };
        
        Object[] values = {
            false, false, false, false, false, true, 5, 200, true, true, true, true
        };
        
        for (int i = 0; i < rules.length; i++) {
            world.setGameRule(rules[i], values[i]);
        }
    }

    private void startPercentTask() {
        // Start the percent update task after 4 minutes (4800 ticks)
        int percentTaskId = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {
            public void run() {
                // Create a repeating task that updates percent every second for 18 minutes
                final long startTime = System.currentTimeMillis();
                final long durationMs = 11 * 60 * 1000; // 11 minutes in milliseconds
                
                int updateTaskId = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), new Runnable() {
                    public void run() {
                        if (!gamerunning) {
                            // Game ended, stop updating
                            return;
                        }
                        
                        long currentTime = System.currentTimeMillis();
                        long elapsed = currentTime - startTime;
                        
                        if (elapsed >= durationMs) {
                            // 15 minutes have passed, set to 1.0 and stop
                            percent = 1.0;
                        } else {
                            // Calculate percent based on elapsed time
                            percent = (double) elapsed / durationMs;
                        }
                        
                        // Ensure percent stays within bounds
                        if (percent > 1.0) {
                            percent = 1.0;
                        }
                    }
                }, 0L, 20L); // Run immediately, then every second (20 ticks)
                
                taskIds.add(updateTaskId);
            }
        }, 4800L); // 4 minutes delay (4800 ticks = 4 minutes)
        
        taskIds.add(percentTaskId);
    }

    public void startGame() {
        alivePlayers.clear();
        percent = 0.0;
        multiverseCore = Main.getMultiverseCore();

        if (multiverseCore == null) {
            Bukkit.getLogger().severe("Multiverse-Core not found!");
            return;
        }

        // Send out warning to players
        for (Player warnmessage : Bukkit.getOnlinePlayers()) {
            warnmessage.getWorld().playSound(warnmessage.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_1, 1.0F, 1.0F);
            warnmessage.sendTitle(null, "§aGame starting soon!", 20, 100, 20);
        }
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "title @a title {\"text\":\"\u0030\",\"font\":\"customfont:images\"}");

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {
            public void run() {

                if(multiverseCore.getMVWorldManager().getMVWorld("beangame-world") == null){
                    Bukkit.getLogger().severe("beangame-world doesn't exist, creating new world");
                    createBeangameWorld();
                }
                multiverseCore.getMVWorldManager().getMVWorld("beangame-world").setAutoLoad(true);

                new ResetItems().resetAllItems();
                
                DeathSpectateCommand.deathspectate = false;
                PvpToggleCommand.pvp = false;
                BorderManipulator.BorderManipulatorActive = true;
                BorderManipulator.target = null;
                RandomizerCommand.setRandomizer(false);
                startphase = true;
                gamerunning = true;

                autoroll = true;
                autorollRunCount = 0;
                startAutorollTask();

                startPercentTask();
            }
        }, 200L);

        // Second part of beangame, PvP enabled, world border moving
        int borderTaskId = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.getWorld().playSound(player.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_1, 1.0F, 1.0F);
                    player.sendTitle(null, "§cBorder is closing, & damage is enabled!", 20, 100, 20);
                }
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "title @a title {\"text\":\"\u0030\",\"font\":\"customfont:images\"}");

                BorderManipulator.BorderManipulatorActive = false;
                Bukkit.getWorld("beangame-world").getWorldBorder().setSize(21, 750);
            }
        }, 4500L);
        taskIds.add(borderTaskId); // Store the task ID

        int pvpTaskId = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§fPvP enabled!"));
                }
                PvpToggleCommand.pvp = true;
                DeathSpectateCommand.deathspectate = true;
                startphase = false;
                UltimateGamble.UltimateGambleActive = true;
            }
        }, 4500L);
        taskIds.add(pvpTaskId); // Store the task ID

        int borderManipulatorTaskId = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {
            public void run() {
                BorderManipulator.BorderManipulatorActive = true;
            }
        }, 19500L);
        taskIds.add(borderManipulatorTaskId); // Store the task ID

        // Check for game end condition
        int gameEndTaskId = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), new Runnable() {
            public void run() {
                if (gamerunning && getAlivePlayerCount(Bukkit.getWorld("beangame-world")) <= 1) {
                    gamerunning = false;
                    autoroll = false;
                    stopAutorollTask();
                    LevelingSystem ls = Main.getPlugin().getLevelingSystem();
                    if(getAlivePlayerCount(Bukkit.getWorld("beangame-world")) == 1){
                        String winner = "Nobody";
                        for(Player p : Bukkit.getOnlinePlayers()){
                            if(BeangameStart.alivePlayers.contains(p.getUniqueId())){
                                winner = p.getName();
                                ls.onWin(p);
                                for(ItemStack item : p.getInventory()){
                                    if(item == null || item.getType().equals(Material.AIR)){
                                        continue;
                                    }
                                    if(ItemNBT.hasBeanGameTag(item)){
                                        for(BeangameItem bgitem : BeangameItemRegistry.collection()){
                                            if(ItemNBT.isBeanGame(item, bgitem.getKey())){
                                                String nsk = bgitem.getKey().toString(); // Assuming BeangameItem has an ID field
                                                incrementNumLosses(nsk);
                                                incrementNumWins(nsk);
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            player.getWorld().playSound(player.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_1, 1.0F, 1.0F);
                            player.sendTitle(null, "§2" + winner + " has won!", 20, 100, 20);
                            if(winner != player.getName()){
                                ls.onLoss(player);
                            }
                        }
                    } else {
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            player.getWorld().playSound(player.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_1, 1.0F, 1.0F);
                            player.sendTitle(null, "§2Nobody has won!", 20, 100, 20);
                            ls.onLoss(player);
                        }
                    }
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "title @a title {\"text\":\"\u0030\",\"font\":\"customfont:images\"}");

                    // Delay the reset and teleport players back to the lobby
                    int resetTaskId = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {
                        public void run() {
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                if (lobbyWorld != null) {
                                    player.getInventory().clear();
                                    Location lobbySpawn = lobbyWorld.getSpawnLocation();
                                    player.setGameMode(GameMode.ADVENTURE);
                                    player.teleport(lobbySpawn);
                                }
                            }
                            alivePlayers.clear();
                            multiverseCore.getMVWorldManager().unloadWorld("beangame-world");
                            multiverseCore.getMVWorldManager().deleteWorld("beangame-world");

                            startphase = false;
                            PvpToggleCommand.pvp = false;

                            // Cancel all tasks
                            cancelAllTasks();

                            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable(){
                                public void run(){
                                    createBeangameWorld();
                                }
                            }, 45L);
                        }
                    }, 100L); // 5-second delay (100 ticks = 5 seconds)
                    taskIds.add(resetTaskId); // Store the task ID
                }
            }
        }, 400L, 40L); // Check every 2 seconds (40 ticks)
        taskIds.add(gameEndTaskId); // Store the task ID
    }

    private void cancelAllTasks() {
        for (int taskId : taskIds) {
            Bukkit.getScheduler().cancelTask(taskId); // Cancel each task
        }
        taskIds.clear(); // Clear the list of task IDs
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if(sender instanceof Player){
            Player player = (Player) sender;
            if(player.hasPermission("bg.use") && !gamerunning){
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§6Started game!"));
                startGame();
            } else {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§cYou don't have permission to use this command!"));
            }
        }
        return false;
    }

    private int getAlivePlayerCount(World w){
        int count = 0;
        for(Player p : Bukkit.getOnlinePlayers()){
            if((p.getGameMode() == GameMode.SURVIVAL || Revive.noRevive.contains(p.getUniqueId())) && p.getWorld().equals(w)){
                count++;
            }
        }
        return count;
    }

    private void incrementNumWins(String nsk){
        String query = "UPDATE items SET num_wins = num_wins + 1 WHERE key_name = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)){
            statement.setString(1, nsk); // Set the item ID parameter
            statement.executeUpdate(); // Execute the update
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception appropriately
        }
    }

    private void incrementNumLosses(String nsk){
        String query = "UPDATE items SET num_losses = num_losses + 1 WHERE key_name = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)){
            statement.setString(1, nsk); // Set the item ID parameter
            statement.executeUpdate(); // Execute the update
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception appropriately
        }
    }
}

