package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;

public class Craig extends BeangameItem implements BGRClickableI {

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        event.setCancelled(true);
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if(onCooldown(uuid)){
            sendCooldownMessage(player);
            return false;
        }
        applyCooldown(uuid);

        Location loc = player.getLocation();
        player.getWorld().playSound(loc, Sound.ENTITY_CHICKEN_EGG, 1.0f, 1.0f);

        spawnChicken(loc, player);
        return true;
    }

    private void spawnChicken(Location center, Player player) {
        // Implementation for spawning and animating Craig the chicken
        Chicken chicken = (Chicken) center.getWorld().spawnEntity(center, EntityType.CHICKEN);

        chicken.setCustomName("Craig");
        chicken.setInvulnerable(true);
        chicken.setSilent(false);
        chicken.setAge(1);

        Main plugin = Main.getPlugin();
        final int[] levetationLevel = {14};
        final int[] ticks = {0};
        final double initialY = center.getY();
        final boolean[] isGrowing = {true};

        int[] taskId = new int[1];
        taskId[0] = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!chicken.isValid() || ticks[0] > 16 * 20) {
                explodeChicken(chicken);
                Bukkit.getScheduler().cancelTask(taskId[0]);
                return;
            }

            if(ticks[0] % 32 == 0 && levetationLevel[0] < 200){
                levetationLevel[0]++;
                chicken.removePotionEffect(PotionEffectType.LEVITATION);
                chicken.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 100, levetationLevel[0] - 1, false, false));
            }
            if(!chicken.hasPotionEffect(PotionEffectType.LEVITATION)){
                chicken.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 100, levetationLevel[0], false, false));
            }

            double currentY = chicken.getLocation().getY();
            double heightProgress = (currentY - initialY) / 10;
            double scale = Math.min(1.0 + (heightProgress * 4), 16);

            if(isGrowing[0]){
                applyScale(chicken, scale);
            }

            isGrowing[0] = breakBlocksAround(chicken, scale / 2);

            ticks[0]++;
        }, 0, 1).getTaskId();
    }

    private void applyScale(Chicken chicken, double scale) {
        chicken.getAttribute(Attribute.SCALE).setBaseValue(scale);
    }

    private boolean breakBlocksAround(Chicken chicken, double scale) {
        Location loc = chicken.getLocation();
        int radius = (int) Math.ceil(scale);

        for (int x = -radius; x <= radius; x++) {
            for (int y = 0; y <= 2 * radius + 1; y++) {
                for (int z = -radius; z <= radius; z++) {
                    
                    Location blockLoc = loc.clone().add(x, y, z);
                    Block block = blockLoc.getBlock(); 

                    Material material = block.getType();
                    if(material == null || material.isAir()){
                        continue;
                    }
                    if(material.toString().endsWith("ORE")){
                        return false;
                    }


                    block.breakNaturally();

                }
            }
        }
        return true;
    }

    private void explodeChicken(Chicken chicken) {
        Location loc = chicken.getLocation();
        World world = loc.getWorld();

        world.createExplosion(loc, 4.0f, false, false, chicken);
        chicken.remove();
    }

    @Override
    public long getBaseCooldown() {
        return 12000L;
    }

    @Override
    public String getId() {
        return "craig";
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
        return "§fCraig";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to summon Craig the chicken.",
            "§9Craig levitates upwards while growing in size,",
            "§9breaking all blocks around him as he ascends.",
            "§9After reaching maximum height, Craig explodes!",
            "",
            "§9Summon",
            "§9Castable",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }

    @Override
    public Material getMaterial() {
        return Material.HONEYCOMB;
    }

    @Override
    public int getCustomModelData() {
        return 102;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of();
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

