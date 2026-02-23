package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.talisman.BGHPTalismanI;
import com.beangamecore.items.type.talisman.BGInvUnstackable;

import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SippingStraw extends BeangameItem implements BGHPTalismanI, BGInvUnstackable {

    @Override
    public void applyTalismanEffects(Player player, ItemStack item) {
        int radius = 3; // base is this number + 1
        Location loc = player.getLocation();
        if (!player.getGameMode().equals(GameMode.SPECTATOR)) {
            radius += count(player);

            boolean[] waterLavaFound = scanAndModifyBlocks(loc, player.getWorld(), radius);
            applyWaterEffectIfFound(waterLavaFound[0], player);
            applyLavaEffectIfFound(waterLavaFound[1], player);
        }
    }

    private boolean[] scanAndModifyBlocks(Location loc, World world, int radius) {
        boolean foundWater = false;
        boolean foundLava = false;
        int r = radius;

        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    boolean[] blockResults = processBlockAtOffset(loc, world, x, y, z);
                    foundWater = foundWater || blockResults[0];
                    foundLava = foundLava || blockResults[1];
                }
            }
        }

        return new boolean[] { foundWater, foundLava };
    }

    private boolean[] processBlockAtOffset(Location loc, World world, int x, int y, int z) {
        Location checkLoc = loc.clone().add(x, y, z);
        Block block = world.getBlockAt(checkLoc);

        boolean foundWater = false;
        boolean foundLava = false;

        boolean[] result = processBlock(block);
        foundWater = result[0];
        foundLava = result[1];

        if (handleWaterloggedBlock(block)) {
            foundWater = true;
        }

        return new boolean[] { foundWater, foundLava };
    }

    private boolean[] processBlock(Block block) {
        boolean foundWater = false;
        boolean foundLava = false;
        Material mat = block.getType();

        if (isWaterMaterial(mat)) {
            foundWater = true;
            setBlockTypeForWater(block, mat);
        } else if (isLavaMaterial(mat)) {
            foundLava = true;
            setBlockTypeForLava(block, mat);
        }

        return new boolean[] { foundWater, foundLava };
    }

    private boolean isWaterMaterial(Material mat) {
        switch (mat) {
            case WATER:
            case BUBBLE_COLUMN:
            case SEAGRASS:
            case TALL_SEAGRASS:
            case KELP:
            case KELP_PLANT:
            case WATER_CAULDRON:
                return true;
            default:
                return false;
        }
    }

    private boolean isLavaMaterial(Material mat) {
        switch (mat) {
            case LAVA:
            case LAVA_CAULDRON:
                return true;
            default:
                return false;
        }
    }

    private void setBlockTypeForWater(Block block, Material mat) {
        switch (mat) {
            case WATER:
            case BUBBLE_COLUMN:
            case SEAGRASS:
            case TALL_SEAGRASS:
            case KELP:
            case KELP_PLANT:
                block.setType(Material.AIR);
                break;
            case WATER_CAULDRON:
                block.setType(Material.CAULDRON);
                break;
            default:
                break;
        }
    }

    private void setBlockTypeForLava(Block block, Material mat) {
        switch (mat) {
            case LAVA:
                block.setType(Material.AIR);
                break;
            case LAVA_CAULDRON:
                block.setType(Material.CAULDRON);
                break;
            default:
                break;
        }
    }

    private boolean handleWaterloggedBlock(Block block) {
        // Handle waterlogged blocks
        BlockData data = block.getBlockData();
        if (data instanceof Waterlogged) {
            Waterlogged waterlogged = (Waterlogged) data;
            if (waterlogged.isWaterlogged()) {
                waterlogged.setWaterlogged(false);
                block.setBlockData(waterlogged);
                return true;
            }
        }
        return false;
    }

    private void applyWaterEffectIfFound(boolean foundWater, Player player) {
        if (foundWater) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 5, 0)); // 5 seconds, speed
            if (Math.random() > 0.95) {
                player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_DRINK, 0.4f, 1.2f);
            }
        }
    }

    private void applyLavaEffectIfFound(boolean foundLava, Player player) {
        if (foundLava) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20 * 5, 0)); // 5 seconds,
                                                                                                   // fire
                                                                                                   // resistance
            if (Math.random() > 0.95) {
                player.playSound(player.getLocation(), Sound.ENTITY_STRIDER_EAT, 0.6f, 0.8f);
            }
        }
    }

    public int count(Player player){
        AtomicInteger force = new AtomicInteger(0);
        for(ItemStack item : player.getInventory().getContents()){
            if(this.asItem().isSimilar(item)){
                force.set(force.get() + 1);
            }
        }
        return force.get();
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        return null;
    }

    @Override
    public long getBaseCooldown() {
        return 102;
    }

    @Override
    public String getId() {
        return "sippingstraw";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public String getName() {
        return "§fSipping Straw";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§3Drinks all water and lava within",
            "§33+ blocks while held. Grants speed",
            "§3from water and fire resistance from",
            "§3lava. Radius increases with each",
            "§3additional straw carried.",
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
        return Material.BREEZE_ROD;
    }

    @Override
    public int getCustomModelData() {
        return 102;
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

