package com.beangamecore.items;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import com.beangamecore.Main;
import com.beangamecore.entities.rat.CheeseTouchRat;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.damage.entity.BGDDealerInvI;

public class CheeseTouch extends BeangameItem implements BGDDealerInvI {
    
    private static final int NUMBER_OF_RATS = 12;
    private static final double RADIUS = 4.0;

    @Override
    public void attackerInventoryOnHit(EntityDamageByEntityEvent event, ItemStack stack) {
        LivingEntity attacker = (LivingEntity) event.getDamager();
        UUID uuid = attacker.getUniqueId();

        if (onCooldown(uuid)) {
            return;
        }

        applyCooldown(uuid);
        LivingEntity entity = (LivingEntity) event.getEntity();
        Location eloc = entity.getLocation();

        int[] ticks = {0};
        int[] taskId = new int[1];

        taskId[0] = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(Main.class), () -> {
            if (ticks[0] >= NUMBER_OF_RATS) {
                Bukkit.getScheduler().cancelTask(taskId[0]);
                return;
            }

            // Calculate the angle for this tick
            double angle = 2 * Math.PI * ticks[0] / NUMBER_OF_RATS;
            // Calculate the x and z positions based on the angle and radius
            double x = eloc.getX() + RADIUS * Math.cos(angle);
            double z = eloc.getZ() + RADIUS * Math.sin(angle);

            // Find the highest block at the calculated x and z coordinates
            Location newLocation = new Location(eloc.getWorld(), x, eloc.getWorld().getHighestBlockYAt((int) x, (int) z) + 1, z);

            // Summon the CheeseTouchRat at the new location
            CheeseTouchRat.summon(newLocation, entity, attacker);

            ticks[0]++;
        }, 0, 1).getTaskId();
    }

    public void cheesetouchUpdateRats() {
        Set<CheeseTouchRat> toRemove = new HashSet<>();
        Iterator<CheeseTouchRat> iterator = CheeseTouchRat.rats.iterator();
        
        while(iterator.hasNext()){
            CheeseTouchRat rat = iterator.next();
            // rat.ratParticles(); // Commented out for performance, uncomment if needed
            rat.tickRats();
            if(rat.getTicksAlive() > 20 * 20 || !rat.getSilverfish().isValid()){
                toRemove.add(rat);
            }
        }
        // Remove the elements outside of the iteration
        for (CheeseTouchRat rat : toRemove) {
            rat.remove();
            CheeseTouchRat.rats.remove(rat);
        }
    }

    @Override
    public long getBaseCooldown() {
        return 22000L;
    }

    @Override
    public String getId() {
        return "cheesetouch";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, " E ", "EBE", " S ", r.mCFromMaterial(Material.END_STONE), r.eCFromBeangame(Key.bg("bean")), r.eCFromBeangame(Key.bg("spawncore")));
        return null;
    }

    @Override
    public String getName() {
        return "§eCheese Touch";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Hitting an enemy summons 12 rats in",
            "§9a circle around them that attack the",
            "§9victim for 20 seconds. Rats spawn",
            "§9at the highest block around the target.",
            "",
            "§cOn Hit",
            "§9Summon",
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
        return Material.GOLD_INGOT;
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
