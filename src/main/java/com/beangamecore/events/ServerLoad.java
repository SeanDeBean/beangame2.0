package com.beangamecore.events;

import java.util.*;

import com.beangamecore.blocks.generic.BeangameBlock;
import com.beangamecore.blocks.type.BGHPTickableB;
import com.beangamecore.blocks.type.BGLPTickableB;
import com.beangamecore.blocks.type.BGMPTickableB;
import com.beangamecore.commands.BeangameDistribute;
import com.beangamecore.commands.BeangameStart;
import com.beangamecore.commands.DeathSpectateCommand;
import com.beangamecore.commands.PvpToggleCommand;
import com.beangamecore.items.*;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.general.BG1sTickingI;
import com.beangamecore.items.type.general.BG1tTickingI;
import com.beangamecore.items.type.general.BG2tTickingI;
import com.beangamecore.items.type.general.BG30sTickingI;
import com.beangamecore.items.type.general.BG3sTickingI;
import com.beangamecore.items.type.general.BGCyclingI;
import com.beangamecore.registry.BeangameBlockData;
import com.beangamecore.registry.BeangameItemRegistry;
import com.beangamecore.items.type.BGArmorI;
import com.beangamecore.items.type.talisman.BGMPTalismanI;
import com.beangamecore.items.type.talisman.BGHPTalismanI;
import com.beangamecore.items.type.talisman.BGHeldTalismanI;
import com.beangamecore.items.type.talisman.BGInvUnstackable;
import com.beangamecore.items.type.talisman.BGLPTalismanI;
import com.beangamecore.util.*;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import com.beangamecore.Main;

public class ServerLoad implements Listener {

    private static boolean chronobreak = false;
    
    public static Map<UUID, Boolean> sizeadjusted = new HashMap<>();
    public static Team noCollisions;

    private void timer3seconds(){
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), () -> {
            // task to execute every 3 seconds starting 5 seconds after server startup
            for(BeangameItem i : BeangameItemRegistry.collection()){
                i.doIf(BG3sTickingI.class, BG3sTickingI::tick);
            }

            for(World world : Bukkit.getWorlds()){
                Map<Block, BeangameBlock> blocks = BeangameBlockData.getLoadedBeangameBlocks(world);
                blocks.forEach((k, v) -> {
                    if(v instanceof BGLPTickableB b) b.tick(k);
                });
            }
            for(Player player : Bukkit.getOnlinePlayers()){
                PlayerInventory inventory = player.getInventory();
                boolean cloakofthespycheck = false;

                UUID uuid = player.getUniqueId();
                if(!sizeadjusted.containsKey(uuid)){
                    defaultSize(player);
                }
                for(ItemStack i : inventory.getContents()){
                    if(i != null && i.getType() != Material.AIR){
                        if(ItemNBT.hasBeanGameTag(i)){
                            BeangameItemRegistry.getFromItemStack(i).ifPresent(bgitem -> {
                                if(bgitem instanceof BGInvUnstackable u) u.reset(player.getUniqueId(), bgitem);
                            });
                        }
                    }
                }
                for(ItemStack playerInventoryItem : inventory.getContents()){
                    if (playerInventoryItem != null && playerInventoryItem.getType() != Material.AIR){
                        if (ItemNBT.hasBeanGameTag(playerInventoryItem)){
                            BeangameItem item = BeangameItemRegistry.getFromItemStackRaw(playerInventoryItem);
                            if(item instanceof BGInvUnstackable u && u.alreadyActivated(player.getUniqueId(), item)) continue;
                            if(item instanceof BGInvUnstackable u) u.activate(player.getUniqueId(), item);
                            if(item instanceof BGLPTalismanI t) t.applyTalismanEffects(player, playerInventoryItem);
                            if(item instanceof CloakOfTheSpy) cloakofthespycheck = true;
                        }
                    }
                }
                // armor checks
                for(ItemStack stack : player.getEquipment().getArmorContents()){
                    if(ItemNBT.hasBeanGameTag(stack)){
                        BeangameItem item = BeangameItemRegistry.getFromItemStackRaw(stack);
                        if(item instanceof BGInvUnstackable u && u.alreadyActivated(player.getUniqueId(), item)) continue;
                        if(item instanceof BGInvUnstackable u) u.activate(player.getUniqueId(), item);
                        if(item instanceof BGArmorI a) a.applyArmorEffects(player, stack);
                    }
                }

                if((Booleans.getBoolean("cloakofthespy_active", uuid) && !player.isSneaking()) || (Booleans.getBoolean("cloakofthespy_active", uuid) && !cloakofthespycheck)){
                    player.setInvisible(false);
                    Booleans.setBoolean("cloakofthespy_active", player.getUniqueId(), false);
                }
            }
        }, 100L, 60L);
    }

    private void timer1second(){
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), () -> {
            // task to execute every 1 second starting 5 seconds after server startup
            lobbyControls();

            for(World world : Bukkit.getWorlds()){
                Map<Block, BeangameBlock> blocks = BeangameBlockData.getLoadedBeangameBlocks(world);
                blocks.forEach((k, v) -> {
                    if(v instanceof BGMPTickableB b) b.tick(k);
                });
            }
            for(Player player : Bukkit.getOnlinePlayers()){
                if(Longs.getLong("suffocation_stacks", player.getUniqueId()) != 0L && !Cooldowns.onCooldown("suffocation_immunity", player.getUniqueId())){
                    Longs.resetLong("suffocation_stacks", player.getUniqueId());
                }
                PlayerInventory inventory = player.getInventory();
                chronobreak = false;
                for(ItemStack i : inventory.getContents()){
                    if(i != null && i.getType() != Material.AIR){
                        if(ItemNBT.hasBeanGameTag(i)){
                            BeangameItemRegistry.getFromItemStack(i).ifPresent(bgitem -> {
                                if(bgitem instanceof BGInvUnstackable u) u.reset(player.getUniqueId(), bgitem);
                            });
                        }
                    }
                }
                for(ItemStack playerInventoryItem : inventory.getContents()){
                    if (playerInventoryItem != null && playerInventoryItem.getType() != Material.AIR){
                        BeangameItem item = BeangameItemRegistry.getFromItemStackRaw(playerInventoryItem);

                        if(item instanceof BGInvUnstackable u && u.alreadyActivated(player.getUniqueId(), item)) continue;
                        if(item instanceof BGInvUnstackable u) u.activate(player.getUniqueId(), item);
                        if(item instanceof BGMPTalismanI t) t.applyTalismanEffects(player, playerInventoryItem);

                        Material type = playerInventoryItem.getType();
                        Collection<Material> bannedItems = Arrays.asList(ItemCategories.blacklisted);
                        if(bannedItems.contains(type) && !ItemNBT.hasBeanGameTag(playerInventoryItem)){
                            playerInventoryItem.setAmount(0);
                            player.getInventory().addItem(BeangameItemRegistry.getRaw(NamespacedKey.fromString("beangame:beanchronicles")).asItem());
                        }
                    }
                }
                if(!chronobreak){
                    BeangameItemRegistry.get(NamespacedKey.fromString("beangame:chronobreak"), Chronobreak.class).ifPresent(c -> c.chronobreakReset(player));
                }
            }


            for(BeangameItem i : BeangameItemRegistry.collection()){
                i.doIf(BG1sTickingI.class, BG1sTickingI::tick);
            }
        }, 100L, 20L);
    }

    private void timer2ticks() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), () -> {
            for(BeangameItem i : BeangameItemRegistry.collection()){
                i.doIf(BG2tTickingI.class, BG2tTickingI::tick);
            }

            // Queue players needing respawn
            if (BeangameStart.gamerunning) {
                queuePlayersForRespawn();
            }
    
            // Start processing queue if not already running
        }, 100L, 2L);
    }
    
    private void queuePlayersForRespawn() {
        World gameWorld = Bukkit.getWorld("beangame-world");
        if (gameWorld == null) return;
    
        for(Player p : Bukkit.getOnlinePlayers()){
            if (DeathSpectateCommand.deathspectate && !BeangameStart.alivePlayers.contains(p.getUniqueId())){
                if(!p.getWorld().equals(gameWorld)){
                    p.teleport(findSafeSpawnLocation(gameWorld));
                }
                p.setGameMode(GameMode.SPECTATOR);
            } else {
                if(!BeangameStart.alivePlayers.contains(p.getUniqueId())){
                    p.teleport(findSafeSpawnLocation(gameWorld));
                    resetPlayerState(p);
                }
            }
        }
    }

    private Location findSafeSpawnLocation(World world) {
        Location spawnLoc = randomSpawn(null, world);
        
        // Ensure the location is safe
        int attempts = 0;
        while (attempts < 10 && !isLocationSafe(spawnLoc)) {
            spawnLoc = randomSpawn(null, world);
            attempts++;
        }
        return spawnLoc;
    }

    private boolean isLocationSafe(Location loc) {
        Block block = loc.getBlock();
        Block above = loc.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY() + 1, loc.getBlockZ());
        Block below = loc.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ());
        
        return !block.getType().isSolid() && 
            !above.getType().isSolid() && 
            below.getType().isSolid();
    }

    private void resetPlayerState(Player p) {
        BeangameStart.alivePlayers.add(p.getUniqueId());
        p.setHealth(20);
        p.setExp(0);
        p.setTotalExperience(0);
        p.setFoodLevel(20);
        p.getEnderChest().clear();
        p.setRespawnLocation(null);
        p.setGameMode(GameMode.SURVIVAL);
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§fPvP disabled!"));
        p.getInventory().clear();
        p.setAbsorptionAmount(0);
    
        // Remove player from distribute list
        UUID uuid = p.getUniqueId();
        BeangameDistribute.bgdistributePlayers.remove(uuid);
    }

    public static Location randomSpawn(Player player, World world){
        int maxAttempts = 100; // Maximum attempts to find a valid location
        Location center = world.getWorldBorder().getCenter();
        Double radius = world.getWorldBorder().getSize() / 2;
        for (int i = 0; i < maxAttempts; i++) {
            // Generate random coordinates within the world border
            double x = center.getX() + (Math.random() * radius * 2 - radius);
            double z = center.getZ() + (Math.random() * radius * 2 - radius);

            // Get the highest block at the generated coordinates
            int y = world.getHighestBlockYAt((int) x, (int) z);
            Location spawnLocation = new Location(world, x, y + 2, z); // Spawn on top of the block

            // Check if the block below is safe (not water or air)
            Material blockBelow = spawnLocation.clone().subtract(0, 1, 0).getBlock().getType();
            if (blockBelow.isSolid() && blockBelow != Material.WATER && blockBelow != Material.LAVA) {
                return spawnLocation; // Return the safe spawn location
            }
        }
        return null; 
    }

    private void lobbyControls(){
        
        if(!BeangameStart.gamerunning){
            PvpToggleCommand.pvp = false;
            if(Bukkit.getWorld("beangame-world") != null){
                for(Player p : Bukkit.getWorld("beangame-world").getPlayers()){
                    p.teleport(new Location(Bukkit.getWorld("lobby"), 0, 101, 0));
                }
            }
        }

        for(Player p : Bukkit.getWorld("lobby").getPlayers()){
            
            if(p.getGameMode().equals(GameMode.SURVIVAL)){
                p.setGameMode(GameMode.ADVENTURE);
            }
            if(p.getLocation().getY() < 95){
                p.teleport(new Location(Bukkit.getWorld("lobby"), 0, 101, 0));
            }
            if(BeangameStart.gamerunning && DeathSpectateCommand.deathspectate){
                p.teleport(Bukkit.getWorld("beangame-world").getSpawnLocation());
            }
            if(!p.getGameMode().equals(GameMode.CREATIVE)){
                p.getInventory().clear();
                p.getEquipment().setBoots(BeangameItemRegistry.getRaw(Key.bg("carrotslides")).asItem(), true);
            }

        }
    }

    private void timer1tick(){
        // every tick
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), () -> {
            for(World world : Bukkit.getWorlds()){
                Map<Block, BeangameBlock> blocks = BeangameBlockData.getLoadedBeangameBlocks(world);
                blocks.forEach((k, v) -> {
                    if(v instanceof BGHPTickableB b) b.tick(k);
                });
            }
            
            // beangame item tick()
            Main.getPlugin().getSeaCreatureRegistry().tickAllActiveCreatures();

            for(BeangameItem i : BeangameItemRegistry.collection()){
                i.doIf(BG1tTickingI.class, BG1tTickingI::tick);
            }

            Map<BeangameItem, Set<UUID>> currentlyHoldingPlayers = new HashMap<>();
            Set<BeangameItem> allHeldTalismans = new HashSet<>();
            for(BeangameItem item : BeangameItemRegistry.getRegistry().values()){
                if(item instanceof BGHeldTalismanI){
                    allHeldTalismans.add(item);
                }
            }

            for(Player player : Bukkit.getOnlinePlayers()){
                List<ItemStack> held = new ArrayList<>(List.of(
                    player.getEquipment().getItemInMainHand(), 
                    player.getEquipment().getItemInOffHand()
                ));
                for(ItemStack i : held){
                    if(i != null && i.getType() != Material.AIR){
                        if(ItemNBT.hasBeanGameTag(i)){
                            BeangameItemRegistry.getFromItemStack(i).ifPresent(bgitem -> {
                                currentlyHoldingPlayers.computeIfAbsent(bgitem, k -> new HashSet<>()).add(player.getUniqueId());
                                if(bgitem instanceof BGHeldTalismanI r) r.applyHeldTalismanEffects(player, i);
                            });
                        }
                    }
                }
                for(ItemStack i : player.getInventory().getContents()){
                    if(i != null && i.getType() != Material.AIR){
                        if(ItemNBT.hasBeanGameTag(i)){
                            BeangameItemRegistry.getFromItemStack(i).ifPresent(bgitem -> {
                                if(bgitem instanceof BGInvUnstackable u) u.reset(player.getUniqueId(), bgitem);
                            });
                        }
                    }
                }
                for(ItemStack stack : player.getInventory().getContents()){
                    if(ItemNBT.hasBeanGameTag(stack)){
                        BeangameItem item = BeangameItemRegistry.getFromItemStackRaw(stack);
                        if(item instanceof BGInvUnstackable u && u.alreadyActivated(player.getUniqueId(), item)) continue;
                        if(item instanceof BGInvUnstackable u) u.activate(player.getUniqueId(), item);
                        if(item instanceof BGHPTalismanI r) r.applyTalismanEffects(player, stack);
                    }
                }
            }

            // Reset tracking for players no longer holding talisman items
            allHeldTalismans.forEach(bgitem -> {
                Set<UUID> holdingPlayers = currentlyHoldingPlayers.getOrDefault(bgitem, new HashSet<>());
                ((BGHeldTalismanI) bgitem).resetNonHoldingPlayers(holdingPlayers);
            });

            BeangameItemRegistry.get(Key.bg("bordermanipulator"), BorderManipulator.class).ifPresent(bm -> bm.tick(Bukkit.getWorld("beangame-world")));

        }, 100L, 1L);
    }


    private void timer30seconds(){
        // every tick
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), () -> {
            for(BeangameItem i : BeangameItemRegistry.collection()){
                i.doIf(BG30sTickingI.class, BG30sTickingI::tick);
            }
        }, 100L, 20*30L);
    }

    @EventHandler
    private void onStart(org.bukkit.event.server.ServerLoadEvent event){

        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard scoreboard = manager.getMainScoreboard();
        noCollisions = scoreboard.getTeam("noCollisions");
        if(noCollisions == null){
            noCollisions = scoreboard.registerNewTeam("noCollisions");
        }
        noCollisions.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);

        timer30seconds();
        timer3seconds();
        timer1second();
        timer2ticks();
        timer1tick();

        startItemCycles();
    }

    private void startItemCycles(){
        for(BeangameItem i : BeangameItemRegistry.collection()){
            i.doIf(BGCyclingI.class, BGCyclingI::startCycle);
         }
    }

    private void defaultSize(Player player){
        AttributeInstance attribute = player.getAttribute(Attribute.SCALE);
        if(player.getDisplayName().equals("SeanDeBean")) attribute.setBaseValue(0.87);
        else attribute.setBaseValue(1);
        attribute = player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE);
        attribute.setBaseValue(4.5);
        attribute = player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE);
        attribute.setBaseValue(3);
        attribute = player.getAttribute(Attribute.MAX_HEALTH);
        attribute.setBaseValue(20);
        attribute = player.getAttribute(Attribute.MAX_ABSORPTION);
        attribute.setBaseValue(20);
        attribute = player.getAttribute(Attribute.ARMOR);
        attribute.setBaseValue(4);
    }
}

