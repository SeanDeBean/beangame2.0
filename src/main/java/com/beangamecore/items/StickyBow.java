package com.beangamecore.items;

import com.beangamecore.items.generic.BeangameBow;
import com.beangamecore.items.type.talisman.BGLPTalismanI;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class StickyBow extends BeangameBow implements BGLPTalismanI {

    @Override
    public void applyTalismanEffects(Player player, ItemStack item) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAVING, 100, 0, false, false));
    }

    @Override
    public void onProjHit(ProjectileHitEvent event) {
        if (event.getHitEntity() == null) {
            handleBlockHit(event);
            return;
        }
        if (event.getHitEntity() instanceof LivingEntity e) {
            handleLivingEntityHit(event, e);
        }
    }

    private void handleBlockHit(ProjectileHitEvent event) {
        if (Math.random() < 0.45) {
            Block block = event.getEntity().getLocation().add(0, 1, 0).getBlock();
            if (block.getType().equals(Material.AIR)) {
                block.setType(Material.COBWEB);
                event.getHitEntity().remove();
            }
        }
    }

    private void handleLivingEntityHit(ProjectileHitEvent event, LivingEntity e) {
        if(e.getPotionEffect(PotionEffectType.WEAVING) != null){
            return;
        }
        if (shouldStickyEffectApply(event, e)) {
            e.getLocation().getBlock().setType(Material.COBWEB);
        }
        Player p = (Player) event.getEntity().getShooter();
        UUID uuid = p.getUniqueId();
        // cooldown system
        if (onCooldown(uuid)) {
            return;
        }
        applyCooldown(uuid);
        e.getLocation().getBlock().setType(Material.COBWEB);
    }

    private boolean shouldStickyEffectApply(ProjectileHitEvent event, LivingEntity e) {
        return Math.random() < 0.35D && event.getEntity().getLocation().getBlock().getType().equals(Material.AIR);
    }

    @Override
    public long getBaseCooldown() {
        return 12000L;
    }

    @Override
    public String getId() {
        return "stickybow";
    }

    @Override
    public boolean isInItemRotation() {
        return false;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "CCC", "CBS", "CCC", r.mCFromMaterial(Material.COBWEB), r.mCFromMaterial(Material.BOW), r.eCFromBeangame(Key.bg("stickysword")));
        return null;
    }

    @Override
    public String getName() {
        return "§cSticky Bow";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§eArrows have a chance to place cobwebs",
            "§ewhen they hit blocks or entities.",
            "§eGrants Weaving I to the carrier.",
            "",
            "§3Talisman",
            "§eRanged",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.BOW;
    }

    @Override
    public int getCustomModelData() {
        return 0;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
    }

    @Override
    public ArmorTrim getArmorTrim() {
        return null;
    }

    @Override
    public Color getColor() {
        return null;
    }

    @Override
    public int getArmor() {
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

