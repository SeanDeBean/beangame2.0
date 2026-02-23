package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGSneakInvI;
import com.beangamecore.util.Booleans;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class CloakOfTheSpy extends BeangameItem implements BGSneakInvI {
    @Override
    public void onToggleInventoryItemSneak(PlayerToggleSneakEvent event, BeangameItem item){
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if(!player.isSneaking()){
            player.setInvisible(true);
            Booleans.setBoolean("cloakofthespy_active", uuid, true);
        } else if(player.isInvisible() && player.isSneaking()) {
            player.setInvisible(false);
            Booleans.setBoolean("cloakofthespy_active", uuid, false);
        }
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "cloakofthespy";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "FEF", "WBW", "WSW", r.mCFromMaterial(Material.DISC_FRAGMENT_5), r.mCFromMaterial(Material.ENDER_EYE), r.mCFromMaterial(Material.PURPLE_WOOL), r.eCFromBeangame(Key.bg("bean")), r.mCFromMaterial(Material.STRING));
        return null;
    }

    @Override
    public String getName() {
        return "§8Cloak of The Spy";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§3Toggle invisibility by crouching while",
            "§3this item is in your inventory. Become",
            "§3visible when standing and invisible",
            "§3when sneaking.",
            "",
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
        return Material.PURPLE_DYE;
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

