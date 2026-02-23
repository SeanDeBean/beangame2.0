package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGConsumableI;
import com.beangamecore.items.type.BeangameSoftItem;
import com.beangamecore.items.type.damage.BGLateDamageInvI;
import com.beangamecore.items.type.talisman.BGInvUnstackable;
import com.beangamecore.items.type.talisman.BGLPTalismanI;
import com.beangamecore.registry.BeangameItemRegistry;
import com.beangamecore.util.Cooldowns;
import com.beangamecore.util.ItemNBT;

public class Egocentrism extends BeangameItem implements BGConsumableI, BGLateDamageInvI, BGLPTalismanI, BGInvUnstackable {

    public void explode(Player user){
        Location loc = user.getLocation();
        UUID uuid = user.getUniqueId();
        if (Cooldowns.onCooldown("use_item", uuid)){
            count(user, true);
            return;
        }
        AtomicInteger force = new AtomicInteger(count(user, true));
        Block block = loc.getBlock();
        block.setType(Material.END_PORTAL_FRAME);
        for(int i = 0; i < 10; i++){
            Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
                if(block.getType().equals(Material.CAKE)){
                    block.getWorld().spawnParticle(Particle.FLAME, block.getLocation().add(0.5, 0, 0.5), 1);
                }
                if(Math.random() >= 0.75){
                    block.getWorld().playSound(block.getLocation(), Sound.ENTITY_TNT_PRIMED, 1, 1);
                }
            }, 3*i);
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
            block.setType(Material.AIR);
        }, 29);
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
            loc.getWorld().createExplosion(loc, Math.min(force.get()/1.2F, 15), true, true);
        }, 30);
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
            loc.getWorld().createExplosion(loc, Math.min(force.get()/1.2F, 15), true, true);
        }, 1);
    }

    public int count(Player player, boolean remove) {
        AtomicInteger force = new AtomicInteger(0);
        countItems(player.getInventory().getContents(), force, remove);
        countItems(player.getEquipment().getArmorContents(), force, remove);
        return force.get();
    }

    private void countItems(ItemStack[] items, AtomicInteger force, boolean remove) {
        for (ItemStack item : items) {
            if (item != null && ItemNBT.hasBeanGameTag(item)) {
                processItemStack(item, force, remove);
            }
        }
    }

    private void processItemStack(ItemStack item, AtomicInteger force, boolean remove) {
        BeangameItem bgitem = BeangameItemRegistry.getRaw(ItemNBT.getBeanGame(item));
        if (bgitem instanceof BeangameSoftItem) {
            return;
        }

        if (this.getId().equals(bgitem.getId())) {
            force.incrementAndGet();
            if (remove) {
                item.setAmount(0);
            }
        }
    }

    @Override
    public void applyTalismanEffects(Player player, ItemStack item) {
        for(int i = 1; i <= 6; i++){
            Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
                player.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, player.getLocation(), count(player, false), 0, 0.2, 0, 0.1);
            }, 10*i);
        }
        for (ItemStack invitem : player.getInventory().getContents()) {
            // Check if the item has a BeanGame tag
            if (ItemNBT.hasBeanGameTag(invitem)) {
                // Skip processing if the item is a BeangameSoftItem
                BeangameItem bgitem = BeangameItemRegistry.getRaw(ItemNBT.getBeanGame(invitem));
                if (bgitem instanceof BeangameSoftItem) {
                    continue;
                }

                if(this.getId().equals(bgitem.getId())) {
                    continue;
                }
        
                // Remove the item and handle replacement logic
                invitem.setAmount(0);
                ItemStack ego = this.asItem();
                if (player.getInventory().firstEmpty() != -1) {
                    player.getInventory().addItem(ego);
                } else {
                    player.getWorld().dropItemNaturally(player.getLocation(), ego);
                }
                return;
            }
        }
    }

    @Override
    public void onConsume(PlayerItemConsumeEvent event) {
        Player p = event.getPlayer();
        if (onCooldown(p.getUniqueId())){
            sendCooldownMessage(p);
            return;
        }
        explode(p);
    }

    @Override
    public boolean onLateDamageInventory(EntityDamageEvent event, ItemStack item) {
        Player player = (Player) event.getEntity();
        UUID uuid = player.getUniqueId();
        if (onCooldown(uuid)){
            return false;
        }
        if(event.getFinalDamage() > player.getHealth() + player.getAbsorptionAmount()){
            if(event.isCancelled()) return false;
            explode(player);
        }
        return true;

    }

    @Override
    public long getBaseCooldown() {
        return 30000L;
    }

    @Override
    public String getId() {
        return "egocentrism";
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
        return "§bEgocentrism";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§2Eating or taking fatal damage causes",
            "§2a powerful explosion. Explosion size",
            "§2scales with additional Egocentrism items",
            "§2in inventory. Converts other beangame",
            "§2items into Egocentrism over time.",
            "",
            "§2Food",
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
        return Material.SPIDER_EYE;
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

