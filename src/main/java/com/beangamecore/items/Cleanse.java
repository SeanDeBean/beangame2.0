package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.util.Cooldowns;
import com.beangamecore.util.PotionCategories;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Cleanse extends BeangameItem implements BGRClickableI {
    
    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack item) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Location loc = player.getLocation();
        
        // Early return for cooldown check
        if (onCooldown(uuid)) {
            sendCooldownMessage(player);
            return false;
        }
        
        // Apply cooldown
        applyCooldown(uuid);

        // Handle the item event
        player.getWorld().playSound(loc, Sound.ENTITY_FOX_SLEEP, 1.0F, 1.0F);
        
        // Remove harmful potion effects in one step using a loop
        PotionCategories.getHarmfulPotions().forEach(player::removePotionEffect);

        // Clear fire and custom status
        player.setFireTicks(0);

        for (String harmfulCustomPotions : PotionCategories.getHarmfulCustomPotions()) {
            Cooldowns.setCooldown(harmfulCustomPotions, uuid, 0);
        }

        animateCleanse(player);

        return true;
    }

    private void animateCleanse(Player player) {
        World world = player.getWorld();
        UUID playerId = player.getUniqueId();

        // Initial spawn position: 2.5 blocks above player's feet
        Location startLoc = player.getLocation().clone().add(0, 2.1, 0);
        ItemDisplay display = (ItemDisplay) world.spawnEntity(startLoc, EntityType.ITEM_DISPLAY);
        display.setItemStack(new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE));
        display.setBillboard(Display.Billboard.FIXED);
        display.setBrightness(new Display.Brightness(15, 15));
        display.setTeleportDuration(1);

        // Set the display to be horizontal using a 90° rotation on the X axis
        display.setTransformation(new Transformation(
            new Vector3f(0, 0, 0), // translation
            new Quaternionf().rotateX((float) Math.toRadians(90)), // left rotation
            new Vector3f(1.0f, 1.0f, 1.0f), // scale
            new Quaternionf() // right rotation
        ));

        int[] ticks = {0};
        float[] offsetY = {2.1f};
        int[] taskId = new int[1];

        taskId[0] = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
            if (!display.isValid() || ticks[0] >= 20) {
                    display.remove();
                    Bukkit.getScheduler().cancelTask(taskId[0]);
                    return;
                }

                Player p = Bukkit.getPlayer(playerId);
                if (p == null || !p.isOnline()) {
                    display.remove();
                    Bukkit.getScheduler().cancelTask(taskId[0]);
                    return;
                }

                Location playerLoc = p.getLocation().clone().add(0, offsetY[0], 0);
                playerLoc.setYaw(0f);
                playerLoc.setPitch(0f);
                display.teleport(playerLoc);

                offsetY[0] -= 0.1f;
                ticks[0]++;
        }, 0, 1).getTaskId();

        DustOptions whiteDust = new DustOptions(Color.fromRGB(255, 255, 255), 1.2f);

        for (int j = 0; j < 5; j++) { // spawn 5 particles each tick
            double offsetX = (Math.random() - 0.5) * 1.0; // random -0.5 to 0.5
            double offsetYValue = Math.random() * 1.5;        // random 0 to 1.5 (height)
            double offsetZ = (Math.random() - 0.5) * 1.0; // random -0.5 to 0.5

            Location particleLoc = player.getLocation().clone().add(offsetX, offsetYValue, offsetZ);

            world.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, 0, whiteDust);
        }

    }
    
    @Override
    public long getBaseCooldown() {
        return 6000L;
    }

    @Override
    public String getId() {
        return "cleanse";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "DWD", "WBW", "DWD", r.mCFromMaterial(Material.YELLOW_DYE), r.mCFromMaterial(Material.YELLOW_WOOL), r.eCFromBeangame(Key.bg("bleach")));
        return null;
    }

    @Override
    public String getName() {
        return "§eCleanse";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to remove all negative",
            "§9potion effects, extinguish fire, and",
            "§9clear custom status effects like",
            "§9silence, immobilization, and jumbling.",
            "",
            "§9Castable",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.SPONGE;
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

