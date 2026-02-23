package com.beangamecore.items;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class FateFumbler extends BeangameItem implements BGRClickableI {
    
    private final Map<UUID, Long> cooldownEndTimes = new ConcurrentHashMap<>();
    
    // Rift of Reversal constants
    private static final int RIFT_MARK_RADIUS = 15;
    private static final int RIFT_MAX_TARGETS = 8;
    private static final int RIFT_SWAP_DELAY = 2 * 20; // 2 seconds
    private static final int BASE_COOLDOWN = 42000; // 30 seconds
    
    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        // Check cooldown
        if (onCooldown(uuid)) {
            sendCooldownMessage(player);
            return false;
        }
        
        // Activate Fumble Fate
        activateFumbleFate(player);
        
        
        return true;
    }
    
    private void activateFumbleFate(Player player) {
        // Mark targets
        Set<LivingEntity> markedEntities = new HashSet<>();
        
        for (Entity entity : player.getNearbyEntities(RIFT_MARK_RADIUS, RIFT_MARK_RADIUS, RIFT_MARK_RADIUS)) {
            if (entity instanceof LivingEntity living && 
                !entity.equals(player) && 
                !(entity instanceof ArmorStand) &&
                !living.isDead() &&
                markedEntities.size() < RIFT_MAX_TARGETS) {

                if(living instanceof Player targetPlayer){
                    if(targetPlayer.getGameMode() == GameMode.SPECTATOR || targetPlayer.getGameMode() == GameMode.CREATIVE){
                        continue;
                    }
                } 
                
                markedEntities.add(living);
                
                // Marking effect
                player.getWorld().playSound(living.getLocation(), Sound.BLOCK_BELL_USE, 0.2f, 0.5f);
                animateFumbleMarking(living);
            }
        }
        
        if (markedEntities.size() < 2) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacy("§9Need 2+ targets to fumble"));
            return;
        }

        applyCooldown(player.getUniqueId());
        
        // Play silly sound
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 0.5f, 0.8f);
        
        // Delay before fumble
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            performFumbleFate(player, new ArrayList<>(markedEntities));
        }, RIFT_SWAP_DELAY);
    }
    
    private void animateFumbleMarking(LivingEntity entity) {
        int[] tick = {0};
        int[] taskId = new int[1];
        final int duration = 20;

        taskId[0] = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
            if (tick[0] >= duration || entity.isDead()) {
                Bukkit.getScheduler().cancelTask(taskId[0]);
                return;
            }
            
            Location loc = entity.getLocation().add(0, 0.5, 0);
            World world = loc.getWorld();
            
            // Silly question mark particles
            double angle = tick[0] * 0.3;
            double radius = 0.8 + Math.sin(tick[0] * 0.2) * 0.2;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            
            Location particleLoc = loc.clone().add(x, Math.sin(tick[0] * 0.3) * 0.3, z);
            
            // Rainbow colors for silliness
            int colorCycle = tick[0] % 6;
            Color color;
            switch (colorCycle) {
                case 0: color = Color.RED; break;
                case 1: color = Color.ORANGE; break;
                case 2: color = Color.YELLOW; break;
                case 3: color = Color.GREEN; break;
                case 4: color = Color.BLUE; break;
                default: color = Color.PURPLE; break;
            }
            
            world.spawnParticle(Particle.DUST, particleLoc, 0,
                new Particle.DustOptions(color, 1.2f));
            
            // Occasional note particles
            if (tick[0] % 5 == 0) {
                world.spawnParticle(Particle.NOTE, loc.clone().add(0, 1, 0), 1, 0.2, 0.2, 0.2, 0);
            }
            
            tick[0]++;
        }, 0L, 1L).getTaskId();
    }
    
    private void performFumbleFate(Player player, List<LivingEntity> markedEntities) {
        World world = player.getWorld();
        
        // Store original positions
        List<Location> originalPositions = new ArrayList<>();
        for (LivingEntity entity : markedEntities) {
            originalPositions.add(entity.getLocation().clone());
        }
        
        // Shuffle positions (the fumble!)
        List<Location> shuffledPositions = new ArrayList<>(originalPositions);
        Collections.shuffle(shuffledPositions);
        
        // Teleport all entities
        for (int i = 0; i < markedEntities.size(); i++) {
            LivingEntity entity = markedEntities.get(i);
            Location newPos = shuffledPositions.get(i);
            
            // Preserve rotation
            newPos.setYaw(entity.getLocation().getYaw());
            newPos.setPitch(entity.getLocation().getPitch());
            
            // Fumble teleport!
            entity.teleport(newPos);
            
            // Create fumble effect at original location
            createFumbleEffect(originalPositions.get(i));
            
            // Confusion effect
            if (entity instanceof Player targetPlayer) {
                targetPlayer.playSound(targetPlayer.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.3f, 1.0f);
            }
        }
        
        // Silly effects
        world.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 0.5f);
        world.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 0.7f, 1.2f);
        
        // Confetti!
        for (Location originalPos : originalPositions) {
            for (int i = 0; i < 10; i++) {
                world.spawnParticle(Particle.HAPPY_VILLAGER, 
                    originalPos.clone().add(0, 1, 0), 
                    2, 0.5, 0.5, 0.5, 0);
            }
        }
        
        // Victory sound
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.3f, 2.0f);
    }
    
    private void createFumbleEffect(Location loc) {
        World world = loc.getWorld();
        
        // Use arrays for mutable variables
        int[] tick = {0};
        final int duration = 15;
        int[] taskId = new int[1];
        
        taskId[0] = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
            if (tick[0] >= duration) {
                Bukkit.getScheduler().cancelTask(taskId[0]);
                return;
            }
            
            // Spiral effect
            double angle = tick[0] * 0.4;
            double radius = 1.5 * (1 - (double)tick[0] / duration);
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            
            Location particleLoc = loc.clone().add(x, 0.1, z);
            
            // Fade from yellow to red
            int red = 255;
            int green = 255 - (tick[0] * 15);
            int blue = 0;
            
            world.spawnParticle(Particle.DUST, particleLoc, 0,
                new Particle.DustOptions(Color.fromRGB(
                    Math.min(255, red),
                    Math.max(0, green),
                    blue
                ), 1.5f));
            
            // Occasional "poof" particles
            if (tick[0] % 3 == 0) {
                world.spawnParticle(Particle.CLOUD, loc, 1, 0.2, 0.2, 0.2, 0.01);
            }
            
            tick[0]++;
        }, 0L, 1L).getTaskId();
        
        world.playSound(loc, Sound.BLOCK_NOTE_BLOCK_CHIME, 0.3f, 1.5f);
    }
    
    @Override
    public long getBaseCooldown() {
        return BASE_COOLDOWN;
    }
    
    @Override
    public String getId() {
        return "fatefumbler";
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
        return "§6Fate Fumbler";
    }
    
    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right click to mark up to 8 nearby",
            "§9entities. After 2 seconds, randomly",
            "§9swap their positions in a chaotic",
            "§9shuffle of fate.",
            "",
            "§9Castable",
            "§9§obeangame"
        );
    }
    
    @Override
    public Material getMaterial() {
        return Material.IRON_SHOVEL;
    }
    
    @Override
    public int getCustomModelData() {
        return 103;
    }
    
    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }
    
    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);
    }
    
    @Override
    public int getMaxStackSize() {
        return 1;
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
    public EquipmentSlotGroup getSlot(){
        return null;
    }
    
    public void clearPlayerData(UUID uuid) {
        cooldownEndTimes.remove(uuid);
    }
    
    public void onDisable() {
        cooldownEndTimes.clear();
    }
}
