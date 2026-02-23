package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGArmorI;
import com.beangamecore.items.type.damage.entity.BGDReceiverInvI;
import org.bukkit.*;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import com.beangamecore.Main;

import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

public class HulaHoopingPants extends BeangameItem implements BGArmorI, BGDReceiverInvI {
    
    @Override
    public void applyArmorEffects(Player player, ItemStack item) {
        UUID uuid = player.getUniqueId();
        AtomicInteger j = new AtomicInteger(0);
        AtomicInteger t = new AtomicInteger(0);
        
        if(player.getGameMode().equals(GameMode.SPECTATOR)){
            return;
        }

        for(int i = 0; i < 360; i += 18){
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
                Location loc = Bukkit.getPlayer(uuid).getLocation();
                World world = loc.getWorld();
                double angle = Math.toRadians(j.get());

                // first ring
                double x = loc.getX() + 4 * Math.cos(angle);
                double y = loc.getY();
                double z = loc.getZ() + 4 * Math.sin(angle);
                Location spawnloc = new Location(world, x, y, z);
                EvokerFangs fang1 = spawnloc.getWorld().spawn(spawnloc, EvokerFangs.class);
                fang1.setOwner(player);

                fang1.setSilent(true);
                world.playSound(spawnloc, Sound.ENTITY_EVOKER_FANGS_ATTACK, 0.1F, 1F);

                //second ring
                x = loc.getX() - 4 * Math.cos(angle);
                z = loc.getZ() - 4 * Math.sin(angle);
                Location spawnloc2 = new Location(world, x, y, z);
                EvokerFangs fang2 = spawnloc2.getWorld().spawn(spawnloc2, EvokerFangs.class);

                fang2.setOwner(player);
                // degree adjustment
                j.set(j.get() + 18);

                fang2.setSilent(true);
                world.playSound(spawnloc2, Sound.ENTITY_EVOKER_FANGS_ATTACK, 0.1F, 1F);

            }, t.get()*3L);
            t.set(t.get() + 1);
        }
    }

    @Override
    public void victimInventoryOnHit(EntityDamageByEntityEvent event, ItemStack stack){
        if(event.getDamager() instanceof EvokerFangs ef){
            if(ef.getOwner().equals(event.getEntity())){
                event.setCancelled(true);
            }
        }
    }
    
    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "hulahoopingpants";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "EEE", "EFE", "E E", r.mCFromMaterial(Material.EMERALD), r.eCFromBeangame(Key.bg("fangflare")));
        return null;
    }

    @Override
    public String getName() {
        return "§bHula Hooping Pants";
    }


    @Override
    public List<String> getLore() {
        return List.of(
            "§6Creates rotating rings of evoker fangs",
            "§6around you that damage enemies. Your",
            "§6own fangs won't harm you.",
            "",
            "§6Armor",
            "§9§obeangame",
            "§9", "§7When on Body:", "§9+3 Armor", "§9+3 Armor Toughness", "§9+1 Knockback Resistance"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:protection", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.NETHERITE_LEGGINGS;
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
        return new ArmorTrim(TrimMaterial.DIAMOND, TrimPattern.COAST);
    }

    @Override
    public Color getColor() {
        return null;
    }

    @Override
    public int getArmor(){
        return 3;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public EquipmentSlotGroup getSlot(){
        return EquipmentSlotGroup.LEGS;
    }
}

