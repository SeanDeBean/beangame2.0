package com.beangamecore.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.damage.entity.BGDDealerInvI;
import com.beangamecore.items.type.death.BGDeathInvI;

public class GhoulbindCharm extends BeangameItem implements BGDDealerInvI, BGDeathInvI {

    @Override
    public void attackerInventoryOnHit(EntityDamageByEntityEvent event, ItemStack stack) {
        
        if (!(event.getEntity() instanceof LivingEntity victim)) return;

        // Apply Slowness
        victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 1)); // 5s

        LivingEntity damager = (LivingEntity) event.getDamager();
        UUID uuid = damager.getUniqueId();
        if (onCooldown(uuid)){
            return;
        }
        applyCooldown(uuid);

        // Start the grabbing animation
        int[] ticks = {0};
        int[] taskId = new int[1];

        taskId[0] = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
            if (!(victim.isValid() && victim.hasPotionEffect(PotionEffectType.SLOWNESS))) {
                Bukkit.getScheduler().cancelTask(taskId[0]);
                return;
            }

            // Continuously get the victim's current location
            Location loc = victim.getLocation().clone().add(0, 0.1, 0);

            // Spawn particles at that location
            victim.getWorld().spawnParticle(
                Particle.SMOKE, loc, 4, 0.2, 0.05, 0.2, 0.01
            );
            victim.getWorld().spawnParticle(
                Particle.SOUL, loc, 2, 0.15, 0.1, 0.15, 0.02
            );

            ticks[0]++;
            if (ticks[0] > 100) Bukkit.getScheduler().cancelTask(taskId[0]); // safety after 5 seconds
        }, 0L, 5L).getTaskId(); // every 5 ticks
    }

    @Override
    public void onDeathInventory(EntityDeathEvent event, ItemStack item) {
        Entity deadEntity = event.getEntity();
        if (!(deadEntity instanceof Player))
            return;
        Player victim = (Player) deadEntity;

        Zombie ghostZombie = spawnGhostZombie(victim);
        equipGhostZombie(ghostZombie, victim, event);
        removeEquippedItemsFromDrops(event, victim);
        spawnGhostlyParticles(ghostZombie);
    }

    private Zombie spawnGhostZombie(Player victim) {
        Location spawnLoc = victim.getLocation();
        Zombie ghostZombie = (Zombie) spawnLoc.getWorld().spawn(spawnLoc, Zombie.class);

        ghostZombie.setCustomName(victim.getName() + "'s ghost");
        ghostZombie.setAdult();
        ghostZombie.setSilent(true);

        ghostZombie.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20 * 60 * 10, 1, false, false));
        ghostZombie.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 60 * 10, 1, false, false));

        return ghostZombie;
    }

    private ItemStack foundWeaponFromInventory = null; // Track weapon found in inventory
    private boolean weaponFromOffhand = false; // Track if weapon came from offhand

    private void equipGhostZombie(Zombie ghostZombie, Player victim, EntityDeathEvent event) {
        // Get victim's equipment
        ItemStack helmet = victim.getEquipment().getHelmet();
        ItemStack chest = victim.getEquipment().getChestplate();
        ItemStack legs = victim.getEquipment().getLeggings();
        ItemStack boots = victim.getEquipment().getBoots();
        ItemStack mainHand = victim.getEquipment().getItemInMainHand();
        ItemStack offHand = victim.getEquipment().getItemInOffHand();

        // Check if main hand is a weapon, if not search inventory
        ItemStack weaponToUse = mainHand;
        ItemStack offHandToUse = offHand;
        foundWeaponFromInventory = null;
        weaponFromOffhand = false;
        
        if (!isWeapon(mainHand)) {
            weaponToUse = findWeaponInInventory(victim);
            if (weaponToUse != null) {
                foundWeaponFromInventory = weaponToUse.clone();
                // If the weapon came from offhand, clear the offhand
                if (offHand != null && offHand.isSimilar(weaponToUse)) {
                    offHandToUse = null;
                    weaponFromOffhand = true;
                }
            }
        }

        // Equip the ghost zombie
        ghostZombie.getEquipment().setHelmet(helmet);
        ghostZombie.getEquipment().setChestplate(chest);
        ghostZombie.getEquipment().setLeggings(legs);
        
        ItemStack depthBoots = boots != null ? boots.clone() : null;
        if (depthBoots != null) {
            depthBoots.addUnsafeEnchantment(Enchantment.DEPTH_STRIDER, 3);
        }
        ghostZombie.getEquipment().setBoots(depthBoots);
        
        ghostZombie.getEquipment().setItemInMainHand(weaponToUse);
        ghostZombie.getEquipment().setItemInOffHand(offHandToUse);
    }

    private boolean isWeapon(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }
        
        Material type = item.getType();
        return type.name().contains("SWORD") || type.name().contains("AXE");
    }

    private ItemStack findWeaponInInventory(Player victim) {
        ItemStack[] inventory = victim.getInventory().getContents();
        
        // First check offhand
        ItemStack offHand = victim.getEquipment().getItemInOffHand();
        if (isWeapon(offHand)) {
            return offHand;
        }
        
        // Then check main inventory
        for (ItemStack item : inventory) {
            if (isWeapon(item)) {
                return item;
            }
        }
        return null;
    }

    private void removeEquippedItemsFromDrops(EntityDeathEvent event, Player victim) {
        List<ItemStack> playerEquipment = getEquippedItems(victim);
        List<ItemStack> drops = event.getDrops();
        
        // Remove equipped items from drops
        drops.removeIf(drop -> isEquippedItem(drop, playerEquipment) || drop.isSimilar(this.asItem()));
        
        // If we found a weapon in inventory, also remove that from drops
        if (foundWeaponFromInventory != null) {
            drops.removeIf(drop -> drop.isSimilar(foundWeaponFromInventory));
        }
    }

    private List<ItemStack> getEquippedItems(Player victim) {
        List<ItemStack> equipment = new ArrayList<>();
        equipment.add(victim.getEquipment().getHelmet());
        equipment.add(victim.getEquipment().getChestplate());
        equipment.add(victim.getEquipment().getLeggings());
        equipment.add(victim.getEquipment().getBoots());
        equipment.add(victim.getEquipment().getItemInMainHand());
        
        // Only add offhand if we didn't take a weapon from it
        if (!weaponFromOffhand) {
            equipment.add(victim.getEquipment().getItemInOffHand());
        }
        
        return equipment;
    }

    private boolean isEquippedItem(ItemStack drop, List<ItemStack> equipment) {
        // Check if drop is similar to any of the equipped items
        return equipment.stream().filter(Objects::nonNull).anyMatch(item -> drop.isSimilar(item));
    }

    private void spawnGhostlyParticles(Zombie ghostZombie) {
        // Spawn ghostly particles around the zombie every tick for 30 seconds
        int[] timer = {0};
        final int maxTime = 20 * 30; // 30 seconds
        int[] taskId = new int[1];

        taskId[0] = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
            if (timer[0]++ >= maxTime || ghostZombie.isDead()) {
                if (!ghostZombie.isDead())
                    ghostZombie.damage(100);
                Bukkit.getScheduler().cancelTask(taskId[0]);
                return;
            }

            Location loc = ghostZombie.getLocation().add(0, 1, 0); // around head level

            // Example particles: SOUL, WITCH, END_ROD - mix for ghostly vibe
            ghostZombie.getWorld().spawnParticle(Particle.SOUL, loc, 3, 0.3, 0.5, 0.3, 0.01);
            ghostZombie.getWorld().spawnParticle(Particle.END_ROD, loc, 2, 0.2, 0.4, 0.2, 0);
            ghostZombie.getWorld().spawnParticle(Particle.WITCH, loc, 1, 0.1, 0.2, 0.1, 0);
        }, 0L, 1L).getTaskId();
    }


    @Override
    public long getBaseCooldown() {
        return 1000L;
    }

    @Override
    public String getId() {
        return "ghoulbindcharm";
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
        return "§3Ghoulbind Charm";
    }

    @Override
public List<String> getLore() {
    return List.of(
        "§3Applies slowness to enemies on hit",
        "§3with a 5 second cooldown. On death,",
        "§3your equipment becomes an invisible",
        "§3ghost zombie that fights for you.",
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
        return Material.BLACK_DYE;
    }

    @Override
    public int getCustomModelData() {
        return 103;
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


