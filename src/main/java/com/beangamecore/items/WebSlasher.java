package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.items.type.talisman.BGLPTalismanI;
import com.beangamecore.util.Cooldowns;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class WebSlasher extends BeangameItem implements BGRClickableI, BGLPTalismanI {

    private double RADIUS = 10.0D;

    @Override
    public void applyTalismanEffects(Player player, ItemStack item) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAVING, 100, 0, false, false));
    }

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        // cooldown system
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (onCooldown(uuid)) {
            sendCooldownMessage(player);
            return false;
        }
        applyCooldown(uuid);
        handleItemEvent(player);

        player.getWorld().getNearbyEntities(player.getLocation(), RADIUS, RADIUS, RADIUS).stream().filter(e -> e instanceof Player).forEach(e -> {
            if (player.getLocation().distanceSquared(e.getLocation()) <= RADIUS * RADIUS) {
                Cooldowns.setCooldown("immobilized", e.getUniqueId(), 0);
            }
        });
        return true;
    }

    private void handleItemEvent(Player player) {
        Location loc = player.getLocation();
        World world = loc.getWorld();
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§fNearby webs cleared!"));
        world.playSound(loc, Sound.ENTITY_SHEEP_SHEAR, 1.0F, 1.0F);
        world.spawnParticle(Particle.SWEEP_ATTACK, loc, 1);
        clearNearbyWebs(loc, world, (int) RADIUS);
    }

    private void clearNearbyWebs(Location loc, World world, int radius) {
        loc.setX(Math.round(loc.getX()));
        loc.setY(Math.round(loc.getY()));
        loc.setZ(Math.round(loc.getZ()));
        for (int x = (int) (loc.getX() - radius); x < loc.getX() + radius; x++) {
            for (int y = (int) (loc.getY() - radius); y < loc.getY() + radius; y++) {
                for (int z = (int) (loc.getZ() - radius); z < loc.getZ() + radius; z++) {
                    clearWebAt(world, x, y, z);
                }
            }
        }
    }

    private void clearWebAt(World world, int x, int y, int z) {
        Material type = world.getBlockAt(x, y, z).getType();
        if (type == Material.COBWEB) {
            world.getBlockAt(x, y, z).setType(Material.AIR);
            world.dropItemNaturally(world.getBlockAt(x, y, z).getLocation(),
                    new ItemStack(Material.COBWEB, 1));
        }
    }

    @Override
    public long getBaseCooldown() {
        return 4000L;
    }

    @Override
    public String getId() {
        return "webslasher";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, " S ", " H ", " B ", r.mCFromMaterial(Material.SHEARS), r.mCFromMaterial(Material.IRON_HOE), r.eCFromBeangame(Key.bg("bean")));
        return null;
    }

    @Override
    public String getName() {
        return "§f§lWeb Slasher";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to clear webs and",
            "§9immobilizations in an area around you ",
            "§9Passively grants immunity to web",
            "§9based beangame items.",
            "",
            "§9Castable",
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
        return Material.STONE_HOE;
    }

    @Override
    public int getCustomModelData() {
        return 0;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
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

