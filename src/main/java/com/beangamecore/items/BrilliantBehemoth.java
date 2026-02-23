package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.damage.entity.BGDDealerInvI;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class BrilliantBehemoth extends BeangameItem implements BGDDealerInvI {

    @Override
    public void attackerInventoryOnHit(EntityDamageByEntityEvent event, ItemStack item) {
        if (!(event.getDamager() instanceof LivingEntity damager)) return;
        UUID uuid = damager.getUniqueId();

        if (onCooldown(uuid)) return;
        applyCooldown(uuid);

        LivingEntity victim = (LivingEntity) event.getEntity();
        Location vloc = victim.getEyeLocation();
        createExplosionAtVictimEye(vloc);

        EquipmentSlot[] validSlots = getValidEquipmentSlots();

        for (EquipmentSlot slot : validSlots) {
            processVictimEquipmentSlot(victim, slot);
        }
    }

    private void createExplosionAtVictimEye(Location vloc) {
        // Explosion at victim's eye
        vloc.getWorld().createExplosion(vloc, 0.6F, false, true, null);
    }

    private EquipmentSlot[] getValidEquipmentSlots() {
        return new EquipmentSlot[] {
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST,
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET,
                EquipmentSlot.HAND,
                EquipmentSlot.OFF_HAND
        };
    }

    private void processVictimEquipmentSlot(LivingEntity victim, EquipmentSlot slot) {
        ItemStack equipment = victim.getEquipment().getItem(slot);

        if (equipment == null)
            return;
        if (equipment.getType().getMaxDurability() <= 0)
            return;

        ItemStack copy = equipment.clone();
        ItemMeta meta = copy.getItemMeta();

        if (!(meta instanceof Damageable damageable))
            return;
        if (meta.isUnbreakable())
            return;

        int newDamage = damageable.getDamage() + 3;
        damageable.setDamage(newDamage);
        copy.setItemMeta(meta);

        if (newDamage >= copy.getType().getMaxDurability()) {
            victim.getEquipment().setItem(slot, null);
            victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
        } else {
            victim.getEquipment().setItem(slot, copy);
        }
    }

    @Override
    public long getBaseCooldown() {
        return 333L;
    }

    @Override
    public String getId() {
        return "brilliantbehemoth";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "   ", "  A", "GB ", r.mCFromMaterial(Material.DIAMOND_AXE), r.mCFromMaterial(Material.GOLDEN_HORSE_ARMOR), r.eCFromBeangame(Key.bg("boomstick")));
        return null;
    }

    @Override
    public String getName() {
        return "§cBrilliant Behemoth";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§3Hitting enemies creates small explosions",
            "§3that rapidly degrade their equipped",
            "§3armor and tools by 3 durability per hit.",
            "",
            "§cOn Hit",
            "§3Talisman",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }

    @Override
    public Material getMaterial() {
        return Material.GOLDEN_HORSE_ARMOR;
    }

    @Override
    public int getCustomModelData() {
        return 101;
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

