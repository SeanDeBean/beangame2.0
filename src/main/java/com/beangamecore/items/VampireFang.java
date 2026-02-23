package com.beangamecore.items;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.damage.entity.BGDDealerInvI;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.util.ItemNBT;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class VampireFang extends BeangameItem implements BGDDealerInvI {

    public boolean carryingVampireFang(Player player){
        for(ItemStack item : player.getInventory().getContents()){
            if(item != null && ItemNBT.hasBeanGameTag(item)){
                if(ItemNBT.isBeanGame(item, getKey())){
                    return true;
                }
            }
        }
        return false;
    }

    private static final Map<UUID, Integer> failCount = new HashMap<>();

    @Override
    public void attackerInventoryOnHit(EntityDamageByEntityEvent event, ItemStack item) {
        LivingEntity attacker = (LivingEntity) event.getDamager();
        UUID uuid = attacker.getUniqueId();
        if (onCooldown(uuid)) return;
        applyCooldown(uuid);

        int count = failCount.getOrDefault(uuid, 0);
        double chance = 0.20 + (count * 0.10); // +10% for each fail
        boolean activated = Math.random() < chance;

        if (activated) {
            failCount.put(uuid, 0); // reset on success

            double health = attacker.getHealth() + event.getDamage() / 1.5;
            AttributeInstance attr = attacker.getAttribute(Attribute.MAX_HEALTH);
            if (health > attr.getValue()) health = attr.getValue();
            attacker.setHealth(health);

            Location aloc = attacker.getLocation();
            Location vloc = event.getEntity().getLocation();
            World world = attacker.getWorld();

            world.playSound(aloc, Sound.ENTITY_FOX_BITE, 1.5F, 1.0F);
            world.spawnParticle(Particle.CRIMSON_SPORE, aloc, 3);
            world.spawnParticle(Particle.CRIMSON_SPORE, vloc, 3);
            Main.getPlugin().getParticleManager().particleTrail(aloc, vloc, 255, 0, 0);
        } else {
            failCount.put(uuid, count + 1);
        }
    }

    public void reset(){
        failCount.clear();
    }

    @Override
    public long getBaseCooldown() {
        return 333;
    }

    @Override
    public String getId() {
        return "vampirefang";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, " A ", "GTG", " S ", r.eCFromBeangame(Key.bg("emotionalsupportanimal")), r.mCFromMaterial(Material.GHAST_TEAR), r.eCFromBeangame(Key.bg("tomeofregeneration")), r.mCFromMaterial(Material.BEETROOT_SOUP));
        return null;
    }

    @Override
    public String getName() {
        return "§4Vampire Fang";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§320% chance to heal for 66% of damage",
            "§3dealt on hit. Chance increases after",
            "§3each failed activation.",
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
        return Material.IRON_NUGGET;
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

