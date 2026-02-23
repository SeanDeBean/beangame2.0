package com.beangamecore.items;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BeangameSoftItem;
import com.beangamecore.items.type.damage.entity.BGDDealerInvI;
import com.beangamecore.items.type.talisman.BGBlockPlaceTalismanI;

import java.util.List;
import java.util.Map;

import com.beangamecore.registry.BeangameItemRegistry;
import com.beangamecore.util.ItemNBT;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class FireGauntlet extends BeangameItem implements BGDDealerInvI, BGBlockPlaceTalismanI {

    @Override
    public boolean onPlace(BlockPlaceEvent event, ItemStack item) {
        if(event.getBlockPlaced().getType().equals(Material.TNT)){
            Block block = event.getBlockPlaced();
            block.setType(Material.AIR);

            Entity tnt = block.getWorld().spawn(block.getLocation().add(0.5, 0.5, 0.5), TNTPrimed.class);
            ((TNTPrimed) tnt).setFuseTicks(64);
            return true;
        }
        return false;
    }

    public boolean hasFireGauntlet(Player player) {
        if (containsFireGauntlet(player.getInventory().getContents())) {
            return true;
        }
        if (containsFireGauntlet(player.getEquipment().getArmorContents())) {
            return true;
        }
        return false;
    }

    private boolean containsFireGauntlet(ItemStack[] items) {
        for (ItemStack item : items) {
            if (isRelevantFireGauntlet(item)) {
                return true;
            }
        }
        return false;
    }

    private boolean isRelevantFireGauntlet(ItemStack item) {
        if (item != null && ItemNBT.hasBeanGameTag(item)) {
            BeangameItem bgitem = BeangameItemRegistry.getRaw(ItemNBT.getBeanGame(item));
            if (bgitem instanceof BeangameSoftItem) {
                return false;
            }
            if (this.getId().equals(bgitem.getId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void attackerInventoryOnHit(EntityDamageByEntityEvent event, ItemStack stack){
        event.getEntity().setFireTicks(Math.max(70, event.getEntity().getFireTicks()));
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "firegauntlet";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, " I ", " G ", "   ", r.eCFromBeangame(Key.bg("ignite")), r.eCFromBeangame(Key.bg("knockupglove")));
        return null;
    }

    @Override
    public String getName() {
        return "§4Fire Gauntlet";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§3Sets enemies on fire for 3.5 seconds",
            "§3when you hit them.",
            "",
            "§cOn Hit",
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
        return Material.NETHERITE_INGOT;
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

