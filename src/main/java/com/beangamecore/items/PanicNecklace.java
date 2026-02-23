package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.damage.BGDamageInvI;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class PanicNecklace extends BeangameItem implements BGDamageInvI {
    
    @Override
    public void onDamageInventory(EntityDamageEvent event, ItemStack item) {
        if(event.isCancelled() || event.getFinalDamage() <= 0) return;

        Player player = (Player) event.getEntity();
        UUID uuid = player.getUniqueId();

        if(onCooldown(uuid)) return;
        Location playerLoc = player.getLocation();
        
        Location teleportLoc = findRandomLocationAroundPlayer(playerLoc, 8);
        if(teleportLoc == null) return;

        applyCooldown(uuid);

        player.setNoDamageTicks(7);

        player.getWorld().playSound(playerLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 0.7F, 1F);
        spawnEndermanParticles(playerLoc);
        player.teleport(teleportLoc);

        player.getWorld().playSound(teleportLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 0.7F, 1F);
        spawnEndermanParticles(teleportLoc);

        applyScreenShake(player, playerLoc, 12);
        applyScreenShake(player, teleportLoc, 12);

        return;
    }

    private Location findRandomLocationAroundPlayer(Location center, int radius) {
        Random random = new Random();
        
        for(int attempt = 0; attempt < 8; attempt++) {
            double offsetX = (random.nextDouble() - 0.5) * 2 * radius;
            double offsetY = (random.nextDouble() - 0.5) * 2 * radius; 
            double offsetZ = (random.nextDouble() - 0.5) * 2 * radius;
            
            Location testLoc = center.clone().add(offsetX, offsetY, offsetZ);
            
            // Check if location is safe
            if(isLocationSafe(testLoc)) {
                return testLoc;
            }
        }
        
        return null;
    }

    private boolean isLocationSafe(Location location) {
        World world = location.getWorld();
        int blockX = location.getBlockX();
        int blockY = location.getBlockY();
        int blockZ = location.getBlockZ();
        
        // Check world bounds
        if(blockY < world.getMinHeight() || blockY >= world.getMaxHeight()) {
            return false;
        }
        
        // Check world border
        WorldBorder worldBorder = world.getWorldBorder();
        if (!worldBorder.isInside(location)) {
            return false;
        }
        
        Material feetBlock = world.getBlockAt(blockX, blockY, blockZ).getType();
        Material bodyBlock = world.getBlockAt(blockX, blockY + 1, blockZ).getType();
        Material headBlock = world.getBlockAt(blockX, blockY + 2, blockZ).getType();
        Material floorBlock = world.getBlockAt(blockX, blockY - 1, blockZ).getType();
        
        return !feetBlock.isSolid() && !bodyBlock.isSolid() && !headBlock.isSolid() && floorBlock.isSolid();
    }

    private void spawnEndermanParticles(Location location){
        World world = location.getWorld();

        for(int i = 0; i < 20; i++){
            double offsetX = (Math.random() - 0.5) * 2;
            double offsetY = Math.random() * 2;
            double offsetZ = (Math.random() - 0.5) * 2;

            world.spawnParticle(Particle.PORTAL, 
            location.getX(), location.getY(), location.getZ(), 1, offsetX, offsetY, offsetZ, 0.5);
        }

        world.spawnParticle(Particle.SMOKE, location, 10, 0.5, 1, 0.5, 0.05);
    }

    private void applyScreenShake(Player player, Location center, int radius){
        for(Player nearby : player.getWorld().getPlayers()) {
            // Skip the original player
            if (nearby.equals(player)) {
                continue;
            }

            // Skip spectators
            if (nearby.getGameMode() == GameMode.SPECTATOR) {
                continue;
            }

            // Check distance (avoid complex combined conditional)
            if (nearby.getLocation().distance(center) > radius) {
                continue;
            }

            applyAdvancedScreenShake(nearby);
            nearby.playSound(nearby.getLocation(), Sound.BLOCK_CONDUIT_AMBIENT, 0.5f, 1.2f);
        }
    }

    private void applyAdvancedScreenShake(Player player) {
        Random random = new Random();
        int durationTicks = 15;
        float baseYawIntensity = 6.0f;
        float basePitchIntensity = 3.0f;
        
        for (int i = 0; i < durationTicks; i++) {
            final int currentTick = i;
            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                if (!player.isOnline()) return;
                
                Location currentLoc = player.getLocation().clone();
                
                float progress = (float) currentTick / durationTicks;
                float intensityMultiplier;
                
                if (progress < 0.3f) {
                    intensityMultiplier = progress / 0.3f;
                } else {
                    intensityMultiplier = 1.0f - ((progress - 0.3f) / 0.7f);
                }
                
                float randomVariance = 0.7f + (random.nextFloat() * 0.6f);
                intensityMultiplier *= randomVariance;
                
                float yawOffset = (random.nextFloat() - 0.5f) * 2 * baseYawIntensity * intensityMultiplier;
                float pitchOffset = (random.nextFloat() - 0.5f) * 2 * basePitchIntensity * intensityMultiplier;
                
                float newYaw = currentLoc.getYaw() + yawOffset;
                float newPitch = Math.max(-90, Math.min(90, currentLoc.getPitch() + pitchOffset));
                
                Location newLookLoc = currentLoc.clone();
                newLookLoc.setYaw(newYaw);
                newLookLoc.setPitch(newPitch);
                
                player.teleport(newLookLoc);
                
                if (currentTick % 3 == 0) {
                    player.getWorld().spawnParticle(Particle.CRIT, player.getEyeLocation(), 2, 0.1, 0.1, 0.1, 0);
                }
                
            }, i);
        }
    }
    
    @Override
    public long getBaseCooldown() {
        return 16000;
    }

    @Override
    public String getId() {
        return "panicnecklace";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, " C ", " F ", "   ", r.eCFromBeangame(Key.bg("crossnecklace")), r.mCFromMaterial(Material.FERMENTED_SPIDER_EYE));
        return null;
    }

    @Override
    public String getName() {
        return "§cPanic Necklace";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§3Warps you out of danger",
            "§3upon taking damage.",
            "",
            "§3Talisman",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.CHAIN;
    }

    @Override
    public int getCustomModelData() {
        return 101;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_ENCHANTS);
    }

    @Override
    public ArmorTrim getArmorTrim(){
        return null;
    }

    @Override
    public Color getColor() {
        return null;
    }

    @Override
    public int getArmor(){
        return 0;
    }

    @Override
    public EquipmentSlotGroup getSlot(){
        return null;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}

