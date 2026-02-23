package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGProjectileI;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.util.Cooldowns;
import org.bukkit.*;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Hook extends BeangameItem implements BGProjectileI, BGRClickableI {

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack){
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        // cooldown system
        if (onCooldown(uuid)){
            sendCooldownMessage(player);
            return false;
        }
        applyCooldown(uuid);
        // item event
        Location loc = player.getLocation();
        player.getWorld().playSound(loc, Sound.BLOCK_ANVIL_LAND, 0.5F, 1.0F);
        Arrow projectile = launchProjectile(this, player, Arrow.class);
        projectile.setVelocity(projectile.getVelocity().multiply(1.3));
        return true;
    }

    @Override
    public void onProjHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        Player shooter = (Player) projectile.getShooter();
        UUID uuid = shooter.getUniqueId();
        event.setCancelled(true);
        projectile.remove();
        Location loc = projectile.getLocation();
        World world = loc.getWorld();
        world.playSound(loc, Sound.BLOCK_ANVIL_LAND, 0.75F, 1.0F);
        double rad = 4;
        DustOptions dustOptions = new DustOptions(Color.fromRGB(255, 255, 255), 2);
        Main.getPlugin().getParticleManager().spawnParticleSphere(loc, rad, Particle.DUST, dustOptions, 150);
        for (Player hookvictim : Bukkit.getOnlinePlayers()) {
            if (isValidHookVictim(hookvictim, world, loc, uuid, rad)) {
                hookvictim.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 100, 0));
                hookvictim.teleport(shooter);
                Cooldowns.setCooldown("immobilized", hookvictim.getUniqueId(), 900L);
            }
        }
    }

    private boolean isValidHookVictim(Player hookvictim, World world, Location loc, UUID shooterUUID, double rad) {
        Location vloc = hookvictim.getLocation();
        // Check if the victim is in the same world and within 4 blocks, not the
        // shooter, and in survival mode
        return vloc.getWorld().equals(world)
                && vloc.distance(loc) < rad
                && !hookvictim.getUniqueId().equals(shooterUUID)
                && hookvictim.getGameMode().equals(GameMode.SURVIVAL);
    }

    @Override
    public long getBaseCooldown() {
        return 13000L;
    }

    @Override
    public String getId() {
        return "hook";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "  I", " RS", "R S", r.eCFromBeangame(Key.bg("immobilizer")), r.mCFromMaterial(Material.BREEZE_ROD), r.mCFromMaterial(Material.STRING));
        return null;
    }

    @Override
    public String getName() {
        return "§dHook";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Shoots a projectile that pulls all",
            "§9nearby players to you within a 4 block",
            "§9radius. Grants absorption to and briefly",
            "§9immobilizes pulled targets.",
            "",
            "§9Castable",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.NETHER_STAR;
    }

    @Override
    public int getCustomModelData() {
        return 0;
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

