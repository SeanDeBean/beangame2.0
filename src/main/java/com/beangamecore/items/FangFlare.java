package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.items.type.damage.entity.BGDReceiverInvI;
import org.bukkit.*;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.util.Vector;

import com.beangamecore.Main;

public class FangFlare extends BeangameItem implements BGRClickableI, BGDReceiverInvI {
    
    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack){
        event.setCancelled(true);
        // cooldown system
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (onCooldown(uuid)){
            sendCooldownMessage(player);
            return false;
        }
        applyCooldown(uuid);
        // item event
        Location startLocation = player.getLocation();
        Vector direction = startLocation.getDirection().normalize();
        World world = startLocation.getWorld();
        AtomicInteger j = new AtomicInteger(1);
        for(int i = 0; i <= 13; i++){
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable(){
                public void run(){
                    Location spawnLocation = startLocation.clone().add(direction.clone().multiply(j.get())).add(0, 0.5, 0);
                    EvokerFangs fang = world.spawn(spawnLocation, EvokerFangs.class);
                    fang.setOwner(player);
                    j.set(j.get() + 1);
                }
            }, i*2L);
        }
        return true;
    }
    
    @Override
    public void victimInventoryOnHit(EntityDamageByEntityEvent event, ItemStack itemStack){
        if(event.getDamager() instanceof EvokerFangs ef){
            if(ef.getOwner().equals(event.getEntity())){
                event.setCancelled(true);
            }
        }
    }

    @Override
    public long getBaseCooldown() {
        return 11000L;
    }

    @Override
    public String getId() {
        return "fangflare";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, " F ", " A ", "   ", r.eCFromBeangame(Key.bg("treat")), r.mCFromMaterial(Material.IRON_AXE));
        return null;
    }

    @Override
    public String getName() {
        return "§2Fang Flare";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click Summons a line of evoker",
            "§9fangs that travel forward. Your own",
            "§9fangs won't harm you.",
            "",
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
        return Material.IRON_AXE;
    }

    @Override
    public int getCustomModelData() {
        return 102;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_UNBREAKABLE);
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

