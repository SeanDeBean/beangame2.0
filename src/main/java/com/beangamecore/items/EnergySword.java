package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.damage.entity.BGDDealerHeldI;

import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import com.beangamecore.commands.PvpToggleCommand;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class EnergySword extends BeangameItem implements BGDDealerHeldI {
    
    @Override
    public void attackerOnHit(EntityDamageByEntityEvent event, ItemStack item) {
        LivingEntity attacker = (LivingEntity) event.getDamager();
        UUID auuid = attacker.getUniqueId();

        if (onCooldown(auuid)) {
            return;
        }
        applyCooldown(auuid);

        LivingEntity victim = (LivingEntity) event.getEntity();
        UUID vuuid = victim.getUniqueId();
        Location loc = victim.getLocation();
        World world = loc.getWorld();

        for (LivingEntity energyswordvictim : world.getLivingEntities()) {
            if (isValidChainTarget(energyswordvictim, loc, vuuid, auuid)) {
                if (isInvalidTargetState(energyswordvictim)) {
                    return;
                }

                notifyChainDamage(energyswordvictim);
                applyChainDamage(energyswordvictim, attacker, victim, world);
            }
        }
    }

    private boolean isValidChainTarget(LivingEntity target, Location sourceLocation, UUID victimUuid,
            UUID attackerUuid) {
        Location targetLocation = target.getLocation();
        return targetLocation.getWorld().equals(sourceLocation.getWorld()) &&
                targetLocation.distance(sourceLocation) < 12.0D &&
                !target.getUniqueId().equals(victimUuid) &&
                !target.getUniqueId().equals(attackerUuid) &&
                PvpToggleCommand.pvp;
    }

    private boolean isInvalidTargetState(LivingEntity target) {
        return target.isDead() ||
                (target instanceof Player && !((Player) target).getGameMode().equals(GameMode.SURVIVAL));
    }

    private void notifyChainDamage(LivingEntity target) {
        if (target instanceof Player) {
            ((Player) target).spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    TextComponent.fromLegacy("§bDamage chained!"));
        }
    }

    private void applyChainDamage(LivingEntity target, LivingEntity attacker, LivingEntity originalVictim,
            World world) {
        target.damage(1, attacker);
        world.spawnParticle(Particle.ELECTRIC_SPARK, target.getLocation(), 3);
        Main.getPlugin().getParticleManager().lightningEffect(
                originalVictim.getEyeLocation(),
                target.getEyeLocation(),
                77, 77, 255);
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "CCC", "CFC", "CBC", r.eCFromBeangame(Key.bg("cosmicingot")), r.eCFromBeangame(Key.bg("cosmicfury")), r.eCFromBeangame(Key.bg("bean")));
        return null;
    }

    @Override
    public long getBaseCooldown() {
        return 2000;
    }

    @Override
    public String getId() {
        return "energysword";
    }

    @Override
    public boolean isInItemRotation() {
        return false;
    }

    @Override
    public String getName() {
        return "§9Energy Sword";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§cHitting enemies chains damage to all",
            "§cnearby living entities within 12 blocks.",
            "§cChained targets take 0.5 hearts damage",
            "§cand display electric particle effects.",
            "",
            "§cOn Hit",
            "§dOn Hit Applier",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:sweeping_edge", 5);
    }

    @Override
    public Material getMaterial() {
        return Material.DIAMOND_SWORD;
    }

    @Override
    public int getCustomModelData() {
        return 101;
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

