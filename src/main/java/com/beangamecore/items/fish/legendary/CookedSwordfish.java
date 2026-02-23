package com.beangamecore.items.fish.legendary;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import com.beangamecore.items.generic.BeangameFish;
import com.beangamecore.items.type.BeangameSoftItem;
import com.beangamecore.items.type.damage.entity.BGDDealerHeldI;

public class CookedSwordfish extends BeangameFish implements BeangameSoftItem, BGDDealerHeldI {

    @Override
    public void attackerOnHit(EntityDamageByEntityEvent event, ItemStack stack){
        event.getEntity().setFireTicks(Math.max(70, event.getEntity().getFireTicks()));
    }

    public CookedSwordfish() {
        setIsFishable(false);
    }

    @Override
    public String getId() {
        return "cookedswordfish";
    }

    @Override
    public String getName() {
        return "§fCooked Swordfish";
    }

    @Override
    public List<String> getLore() {
        return List.of("§9§obeangame");
    }

    @Override
    public Material getMaterial() {
        return Material.IRON_SWORD;
    }

    @Override
    public int getCustomModelData() {
        return 108;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_UNBREAKABLE);
    }
}

