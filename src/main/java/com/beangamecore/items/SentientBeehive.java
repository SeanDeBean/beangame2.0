package com.beangamecore.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Bee;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.damage.entity.BGDDealerInvI;

public class SentientBeehive extends BeangameItem implements BGDDealerInvI {
    
    private static final Map<UUID, List<Bee>> bees = new HashMap<>();
    private static final List<Bee> bees2 = new ArrayList<>();

    @Override
    public void attackerInventoryOnHit(EntityDamageByEntityEvent event, ItemStack stack) {
        LivingEntity victim = (LivingEntity) event.getEntity();
        UUID uuid = event.getDamager().getUniqueId();

        synchronized (bees) {
            handleBeeTargeting(uuid, victim);

            if (onCooldown(uuid)) {
                return;
            }
            applyCooldown(uuid);

            spawnAndManageBee(uuid, victim);
        }
    }

    private void handleBeeTargeting(UUID uuid, LivingEntity victim) {
        if (bees.containsKey(uuid)) {
            for (Bee bee : bees.get(uuid)) {
                bee.setTarget(victim);
            }
        }
    }

    private void spawnAndManageBee(UUID uuid, LivingEntity victim) {
        World world = victim.getWorld();
        Bee bee = world.spawn(victim.getLocation().add(0, 3, 0), Bee.class);
        bee.setTarget(victim);
        bee.setCannotEnterHiveTicks(999999);
        bee.setRemoveWhenFarAway(true);

        bees.computeIfAbsent(uuid, k -> new ArrayList<>()).add(bee);
        bees2.add(bee);

        scheduleBeeRemoval(uuid, bee);
    }

    private void scheduleBeeRemoval(UUID uuid, Bee bee) {
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {
            public void run() {
                synchronized (bees) {
                    if (bee.isValid()) {
                        bee.remove();
                        List<Bee> beeList = bees.get(uuid);
                        if (beeList != null) {
                            beeList.remove(bee);
                            if (beeList.isEmpty()) {
                                bees.remove(uuid);
                            }
                        }
                        bees2.remove(bee);
                    }
                }
            }
        }, 24 * 20L);
    }

    public static void resetStinger(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Bee bee) {
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {
                public void run() {
                    synchronized (bees) {
                        if (bees2.contains(bee) && bee.isValid()) {
                            bee.setHasStung(false);
                        }
                    }
                }
            }, 20L);
        }
    }

    @Override
    public long getBaseCooldown() {
        return 333;
    }

    @Override
    public String getId() {
        return "sentientbeehive";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "HCH", "HBH", "HSH", r.mCFromMaterial(Material.HONEYCOMB), r.eCFromBeangame(Key.bg("cloakofthecobra")), r.mCFromMaterial(Material.BEE_NEST), r.eCFromBeangame(Key.bg("spawncore")));
        return null;
    }

    @Override
    public String getName() {
        return "§eSentient Beehive";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§3Melee hits summon a bee that",
            "§3targets your victim for 24 seconds.",
            "§3Bees can sting repeatedly and all",
            "§3your bees target your latest target.",
            "",
            "§cOn Hit",
            "§3Talisman",
            "§9Summon",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.HONEYCOMB;
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
    public EquipmentSlotGroup getSlot(){
        return null;
    }

    @Override
    public int getArmor(){
        return 0;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}

