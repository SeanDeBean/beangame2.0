package com.beangamecore.items;

import java.util.List;
import java.util.Map;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.items.type.damage.entity.BGDDealerHeldI;

public class Mirror extends BeangameItem implements BGDDealerHeldI, BGRClickableI {

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        event.setCancelled(true);
        return true;
    }

    @Override
    public void attackerOnHit(EntityDamageByEntityEvent event, ItemStack stack) {
        if (!(event.getDamager() instanceof LivingEntity)) {
            return;
        }
        LivingEntity livingEntity = (LivingEntity) event.getDamager();
        if (!(event.getEntity() instanceof Player)
                || livingEntity.getUniqueId().equals(event.getEntity().getUniqueId())) {
            return;
        }
        stack.setAmount(stack.getAmount() - 1);
        Player victim = (Player) event.getEntity();

        EntityEquipment vequipment = victim.getEquipment();
        EntityEquipment aequipment = livingEntity.getEquipment();
        World world = livingEntity.getWorld();
        Location loc = livingEntity.getLocation();

        dropCurrentArmorAndItems(aequipment, world, loc);

        setEquipmentToVictimEquipment(aequipment, vequipment);
    }


    private void dropCurrentArmorAndItems(EntityEquipment aequipment, World world, Location loc) {
        // Drop player's current armor and items
        for (ItemStack armor : aequipment.getArmorContents()) {
            if (armor != null) {
                world.dropItemNaturally(loc, armor);
            }
        }
        ItemStack mainhand = aequipment.getItemInMainHand();
        if (mainhand != null && !mainhand.getType().isAir()) {
            world.dropItemNaturally(loc, aequipment.getItemInMainHand());
        }
        ItemStack offhand = aequipment.getItemInOffHand();
        if (offhand != null && !offhand.getType().isAir()) {
            world.dropItemNaturally(loc, aequipment.getItemInOffHand());
        }
    }

    private void setEquipmentToVictimEquipment(EntityEquipment aequipment, EntityEquipment vequipment) {
        // Set player's equipment to victim's equipment
        aequipment.setArmorContents(vequipment.getArmorContents().clone());
        ItemStack copymainhand = vequipment.getItemInMainHand().clone();
        if (canSetItem(copymainhand)) {
            aequipment.setItemInMainHand(copymainhand);
        }
        ItemStack copyoffhand = vequipment.getItemInOffHand().clone();
        if (canSetItem(copyoffhand)) {
            aequipment.setItemInOffHand(copyoffhand);
        }
    }

    private boolean canSetItem(ItemStack itemStack) {
        return !itemStack.getType().equals(Material.PAINTING) && !itemStack.getType().equals(Material.SHULKER_BOX)
                && !itemStack.getType().equals(Material.BUNDLE);
    }

    @Override
    public long getBaseCooldown() {
        return 0L;
    }

    @Override
    public String getId() {
        return "mirror";
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
        return "§dMirror";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§cMelee hits swap your equipment with",
            "§cthe target's equipment. Drops your",
            "§ccurrent gear and copies their armor,",
            "§cmain hand, and offhand items.",
            "§cSingle-use consumable.",
            "",
            "§cOn Hit",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.PAINTING;
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

