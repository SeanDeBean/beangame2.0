package com.beangamecore.items;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.beangamecore.events.ServerLoad;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGArmorI;
import com.beangamecore.util.BlockCategories;

public class XrayHelmet extends BeangameItem implements BGArmorI {
    
    private static final List<Slime> slimes = new ArrayList<>();

    @Override
    public void applyArmorEffects(Player player, ItemStack item) {
        List<Block> ores = getOres(player.getLocation(), 8);
        World world = player.getWorld();

        if (world == null) return; // Prevent potential NullPointerException

        for (Entity xrayhelmet : player.getNearbyEntities(7, 7, 7)) {
            if (xrayhelmet instanceof LivingEntity le && !le.equals(player) && (le.getPotionEffect(PotionEffectType.INVISIBILITY) != null || le.isInvisible())) {
                ((LivingEntity) xrayhelmet).addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 70, 1));
            }
                
        }

        for (Block ore : ores) {
            Location loc = ore.getLocation().add(0.5, 0.25, 0.5);
            double originalY = loc.getY();
            loc.setY(-70);

            Slime slime = world.spawn(loc, Slime.class);
            configureSlime(slime);

            slimes.add(slime);
            ServerLoad.noCollisions.addEntry(slime.getUniqueId().toString());

            loc.setY(originalY);
            slime.teleport(loc, TeleportCause.PLUGIN);
        }
    }

    private void configureSlime(Slime slime) {
        slime.setInvisible(true);
        slime.setAI(false);
        slime.setAware(false);
        slime.setCollidable(false);
        slime.setGlowing(true);
        slime.setInvulnerable(true);
        slime.setSize(1);
        slime.setSilent(true);
    }

    public static void resetSlimes() {
        Iterator<Slime> iterator = slimes.iterator();
        while (iterator.hasNext()) {
            Slime slime = iterator.next();

            // Ensure slimes older than 70 ticks are removed
            if (slime.getTicksLived() >= 70 || slime.isDead()) {
                slime.remove();
                iterator.remove();
                ServerLoad.noCollisions.removeEntry(slime.getUniqueId().toString());
            }
        }
    }

    private List<Block> getOres(Location loc, int radius) {
        List<Block> ores = new ArrayList<>();
        Set<Material> oreSet = new HashSet<>(BlockCategories.ores); // Use a Set for fast lookup

        int px = loc.getBlockX();
        int py = loc.getBlockY();
        int pz = loc.getBlockZ();

        World world = loc.getWorld();
        if (world == null)
            return ores; // Prevent potential NullPointerException

        scanCubeForOres(world, px, py, pz, radius, oreSet, ores);
        return ores;
    }

    private void scanCubeForOres(World world, int originX, int originY, int originZ, int radius, Set<Material> oreSet,
            List<Block> ores) {
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    processBlock(world, originX, originY, originZ, x, y, z, oreSet, ores);
                }
            }
        }
    }

    private void processBlock(World world, int originX, int originY, int originZ, int offsetX, int offsetY, int offsetZ,
            Set<Material> oreSet, List<Block> ores) {
        Block block = world.getBlockAt(originX + offsetX, originY + offsetY, originZ + offsetZ);
        if (oreSet.contains(block.getType())) {
            ores.add(block);
        }
    }
        
    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "xrayhelmet";
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
        return "§fXray Helmet";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§6Reveals nearby ores and",
            "§6invisible players",
            "",
            "§6Armor",
            "§9§obeangame",
            "",
            "When worn on head:",
            "+1 Armor"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:fire_protection", 3);
    }

    @Override
    public Material getMaterial() {
        return Material.CHAINMAIL_HELMET;
    }

    @Override
    public int getCustomModelData() {
        return 0;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ARMOR_TRIM);
    }

    @Override
    public ArmorTrim getArmorTrim(){
        return new ArmorTrim(TrimMaterial.LAPIS, TrimPattern.EYE);
    }

    @Override
    public Color getColor() {
        return null;
    }

    @Override
    public int getArmor(){
        return 1;
    }

    @Override
    public EquipmentSlotGroup getSlot(){
        return EquipmentSlotGroup.HEAD;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}


