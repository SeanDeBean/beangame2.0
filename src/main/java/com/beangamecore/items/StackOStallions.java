package com.beangamecore.items;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import com.beangamecore.Main;
import com.beangamecore.events.ServerLoad;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.util.Cooldowns;
import com.beangamecore.util.ItemNBT;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class StackOStallions extends BeangameItem implements BGRClickableI {
    private final int horseCount = 10;
    private final int ticksBetweenSpawns = 2;
    private final int ticksAtMax = 40;
    
    // Tracking active horse stacks
    private final Map<UUID, List<List<Horse>>> activeStacks = new HashMap<>();
    private final Map<UUID, List<Integer>> removalTasks = new HashMap<>();
    
    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack itemStack) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        if (onCooldown(uuid)) {
            sendCooldownMessage(player);
            return false;
        }
        applyCooldown(uuid);

        Cooldowns.setCooldown("fall_damage_immunity", uuid, 8000L);
        
        startHorseStackAnimation(player);
        
        return true;
    }

    public int count(Player player){
        AtomicInteger force = new AtomicInteger(0);
        for(ItemStack item : player.getInventory().getContents()){
            if(item != null && ItemNBT.hasBeanGameTag(item) && ItemNBT.isBeanGame(item, getKey())){
                force.set(force.get() + 1);
            }
        }
        return force.get();
    }

    private void startHorseStackAnimation(Player player) {
        UUID uuid = player.getUniqueId();
        Location startLoc = player.getLocation().clone();
        World world = startLoc.getWorld();
        
        world.playSound(startLoc, Sound.ENTITY_HORSE_AMBIENT, 0.8F, 0.8F);
        
        // Create a new stack list for this specific stack
        List<Horse> newStack = new ArrayList<>();
        
        // Initialize the player's stack list if needed
        activeStacks.computeIfAbsent(uuid, k -> new ArrayList<>()).add(newStack);
        removalTasks.computeIfAbsent(uuid, k -> new ArrayList<>());
        
        // Lambda version
        int[] taskId = new int[1];
        int[] currentHorse = {0};
        Horse[] previousHorse = {null};
        Location[] currentSpawnLoc = {startLoc.clone()};
        List<Horse> currentStack = newStack;
        
        taskId[0] = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
            int numHorses = horseCount + count(player) - 1;
            if (currentHorse[0] >= numHorses || !player.isOnline()) {
                Bukkit.getScheduler().cancelTask(taskId[0]);
                scheduleHorseRemoval(uuid, currentStack);
                return;
            }
            
            if (currentHorse[0] == 0) {
                Horse horse = world.spawn(currentSpawnLoc[0], Horse.class);
                currentStack.add(horse);
                
                horse.setAdult();
                horse.setTamed(true);
                horse.setOwner(player);
                
                Horse.Color[] colors = Horse.Color.values();
                Horse.Style[] styles = Horse.Style.values();
                horse.setColor(colors[new Random().nextInt(colors.length)]);
                horse.setStyle(styles[new Random().nextInt(styles.length)]);

                ServerLoad.noCollisions.addEntry(horse.getUniqueId().toString());

                horse.addPassenger(player);
                
                previousHorse[0] = horse;
            } else {
                Location newHorseLoc = currentSpawnLoc[0].clone();
                
                Horse newHorse = world.spawn(newHorseLoc, Horse.class);
                currentStack.add(newHorse);
                
                newHorse.setAdult();
                newHorse.setTamed(true);
                newHorse.setOwner(player);
                
                Horse.Color[] colors = Horse.Color.values();
                Horse.Style[] styles = Horse.Style.values();
                newHorse.setColor(colors[new Random().nextInt(colors.length)]);
                newHorse.setStyle(styles[new Random().nextInt(styles.length)]);
                
                if (previousHorse[0] != null && previousHorse[0].isValid()) {
                    newHorse.addPassenger(previousHorse[0]);
                }
                
                previousHorse[0] = newHorse;
                currentSpawnLoc[0] = newHorseLoc;
            }
            
            world.spawnParticle(Particle.CLOUD, currentSpawnLoc[0], 5, 0.2, 0.3, 0.2, 0.02);
            
            if (currentHorse[0] % 2 == 0) {
                world.playSound(currentSpawnLoc[0], Sound.BLOCK_NOTE_BLOCK_HARP, 0.3F, 
                    (float) (1.0 + (currentHorse[0] * 0.05)));
            }
            
            currentHorse[0]++;
        }, 0L, ticksBetweenSpawns).getTaskId();
    }
    
    private void scheduleHorseRemoval(UUID playerUUID, List<Horse> stack) {
        // Schedule removal after ticksAtMax for this specific stack
        int taskId = Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            removeHorseStack(playerUUID, stack);
        }, ticksAtMax).getTaskId();
        
        removalTasks.get(playerUUID).add(taskId);
    }
    
    private void removeHorseStack(UUID playerUUID, List<Horse> stack) {
        if (stack == null || stack.isEmpty()) {
            cleanupPlayerStack(playerUUID, stack);
            return;
        }
        
        Player player = Bukkit.getPlayer(playerUUID);
        final World world = player != null && player.isOnline() ? player.getWorld() : null;

        // Lambda version
        int[] taskId = new int[1];
        int[] currentIndex = {stack.size() - 1};
        
        taskId[0] = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
            if (currentIndex[0] < 0) {
                Bukkit.getScheduler().cancelTask(taskId[0]);
                cleanupPlayerStack(playerUUID, stack);
                return;
            }
            
            Horse horse = stack.get(currentIndex[0]);
            if (horse != null && horse.isValid()) {
                Location loc = horse.getLocation();
                
                if (world != null) {
                    world.playSound(loc, Sound.ENTITY_HORSE_STEP_WOOD, 0.4F, 1.2F);
                    world.spawnParticle(Particle.SMOKE, loc, 5, 0.2, 0.2, 0.2, 0.05);
                }
                
                horse.remove();
            }
            
            currentIndex[0]--;
        }, 0L, ticksBetweenSpawns).getTaskId();
    }

     private void cleanupPlayerStack(UUID playerUUID, List<Horse> stack) {
        List<List<Horse>> playerStacks = activeStacks.get(playerUUID);
        if (playerStacks != null) {
            playerStacks.remove(stack);
            if (playerStacks.isEmpty()) {
                activeStacks.remove(playerUUID);
            }
        }
    }
    
    public void cleanupAllPlayerStacks(Player player) {
        UUID uuid = player.getUniqueId();
        List<List<Horse>> playerStacks = activeStacks.get(uuid);
        if (playerStacks != null) {
            for (List<Horse> stack : new ArrayList<>(playerStacks)) {
                for (Horse horse : stack) {
                    if (horse != null && horse.isValid()) {
                        horse.remove();
                    }
                }
                stack.clear();
            }
            activeStacks.remove(uuid);
        }
        
        // Cancel all removal tasks for this player
        List<Integer> tasks = removalTasks.get(uuid);
        if (tasks != null) {
            for (Integer taskId : tasks) {
                Bukkit.getScheduler().cancelTask(taskId);
            }
            removalTasks.remove(uuid);
        }
    }
    
    @Override
    public long getBaseCooldown() {
        return 14500L;
    }
    
    @Override
    public String getId() {
        return "stackostallions";
    }
    
    @Override
    public boolean isInItemRotation() {
        return true;
    }
    
    @Override
    public CraftingRecipe getCraftingRecipe() {
        return null;
    }
    
    @Override
    public String getName() {
        return "§6Stack O' Stallions";
    }
    
    @Override
    public List<String> getLore() {
        return List.of(
            "§fRight-click to create a towering stack",
            "§fof horses that scales with more copies.",
            "§fAutomatically ride to the top of the stack.",
            "",
            "§9Castable",
            "§fMovement",
            "§9§obeangame"
        );
    }
    
    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }
    
    @Override
    public Material getMaterial() {
        return Material.SADDLE;
    }
    
    @Override
    public int getCustomModelData() {
        return 0;
    }
    
    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_ENCHANTS);
    }
    
    @Override
    public ArmorTrim getArmorTrim() {
        return null;
    }
    
    @Override
    public Color getColor() {
        return null;
    }
    
    @Override
    public int getArmor() {
        return 0;
    }
    
    @Override
    public EquipmentSlotGroup getSlot() {
        return null;
    }
    
    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
