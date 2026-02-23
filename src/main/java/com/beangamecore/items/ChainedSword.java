package com.beangamecore.items;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.damage.entity.BGDDealerHeldI;
import com.beangamecore.items.type.talisman.BGMPTalismanI;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.util.Vector;

public class ChainedSword extends BeangameItem implements BGDDealerHeldI, BGMPTalismanI {
    
    @Override
    public void applyTalismanEffects(Player player, ItemStack item){
        ItemMeta meta = item.getItemMeta();
        int cmd = meta.getCustomModelData();
        if(cmd == 102){
            cmd = 103;
        } else {
            cmd = 102;
        }
        meta.setCustomModelData(cmd);
        item.setItemMeta(meta);
    }

    @Override
    public long getBaseCooldown() {
        return 2000L;
    }

    @Override
    public void attackerOnHit(EntityDamageByEntityEvent event, ItemStack itemStack) {
        if (event.isCancelled()) {
            return;
        }
        if (!(event.getEntity() instanceof LivingEntity victim))
            return;
        if (!(event.getDamager() instanceof LivingEntity attacker))
            return;
        UUID auuid = attacker.getUniqueId();
        if (onCooldown(auuid)) {
            return;
        }
        applyCooldown(auuid);

        // Particle and sound effects
        Location vloc = victim.getLocation();
        World world = vloc.getWorld();
        if (world != null) {
            playEffectsAtLocation(world, vloc);
            applyAoePullEffect(world, vloc, victim, attacker);
        }
    }

    private void playEffectsAtLocation(World world, Location location) {
        world.playSound(location, Sound.BLOCK_CHAIN_BREAK, 1.0F, 1.0F);
        world.spawnParticle(Particle.SONIC_BOOM, location.add(0, 0.45, 0), 2);
    }

    private void applyAoePullEffect(World world, Location vloc, LivingEntity victim, LivingEntity attacker) {
        // aoe pull effect
        int i = 1;
        for (Entity pullvictims : world.getNearbyEntities(vloc, 6, 6, 6)) {
            i++;
            schedulePullEffect(i, pullvictims, victim, attacker, world);
        }
    }

    private void schedulePullEffect(int delay, Entity pullvictims, LivingEntity victim, LivingEntity attacker,
            World world) {
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {
            public void run() {
                if (!(pullvictims instanceof LivingEntity)) {
                    return;
                }
                LivingEntity pullvictim = (LivingEntity) pullvictims;
                if (pullvictim instanceof Player p) {
                    if (!(p.getGameMode().equals(GameMode.SURVIVAL) || p.getGameMode().equals(GameMode.ADVENTURE))) {
                        return;
                    }
                }
                Location vloc = victim.getLocation();
                Location pvloc = pullvictim.getLocation();
                if (isValidPullTarget(pvloc, vloc, pullvictim, victim, attacker)) {
                    executePullEffect(pullvictim, victim, vloc, pvloc, world);
                }
            }
        }, 3 * delay);
    }

    private boolean isValidPullTarget(Location pvloc, Location vloc, LivingEntity pullvictim, LivingEntity victim,
            LivingEntity attacker) {
        return pvloc.getWorld().equals(vloc.getWorld()) &&
                pvloc.distance(vloc) < 12.0D &&
                !pullvictim.getUniqueId().equals(victim.getUniqueId()) &&
                !pullvictim.getUniqueId().equals(attacker.getUniqueId());
    }

    private void executePullEffect(LivingEntity pullvictim, LivingEntity victim, Location vloc, Location pvloc,
            World world) {
        
        boolean hasKBResistance = false;
        if(pullvictim instanceof Player){
            Player pVictim = (Player) pullvictim;
            hasKBResistance = pVictim.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE) != null && 
                    pVictim.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE).getValue() >= 0.7;
        }
        if (!hasKBResistance) {
            pullvictim.setVelocity(vloc.toVector().subtract(pvloc.toVector()).multiply(0.2));
        }
        pullvictim.damage(0, victim);
        
        // chain particle
        chainEffect(vloc.add(0, 1, 0), pullvictim.getEyeLocation(), 105, 105, 105);
        world.spawnParticle(Particle.SONIC_BOOM, pvloc.add(0, 0.45, 0), 2);
        world.playSound(pvloc, Sound.BLOCK_CHAIN_BREAK, 1.0F, 1.0F);
    }

    private void chainEffect(Location start, Location end, int r, int g, int b) {
        DustOptions dustOptions = new DustOptions(Color.fromRGB(r, g, b), 1);
        Vector direction = end.toVector().subtract(start.toVector());
        double length = direction.length();
        direction.normalize();

        Location current = start.clone();
        Random random = new Random();

        // Calculate the maximum height of the parabola
        double maxHeight = Math.max(start.getY(), end.getY() - 0.1);

        for (double d = 0; d < length; d += 0.125) {
            // Calculate the parabolic offset
            double t = d / length; // Normalized position along the line (0 to 1)
            double parabolicHeight = (4 * maxHeight - 4 * (start.getY() + (end.getY() - start.getY()) * t)) * t * (1 - t); // Upside-down parabola formula

            // Apply the parabolic offset and a small random offset
            Vector offset = new Vector(
                (random.nextDouble() - 0.5) * 0.3, // Horizontal randomness
                parabolicHeight + (random.nextDouble() - 0.5) * 0.5, // Parabolic height + vertical randomness
                (random.nextDouble() - 0.5) * 0.3 // Horizontal randomness
            );

            current = start.clone().add(direction.clone().multiply(d)).add(offset);
            current.getWorld().spawnParticle(Particle.DUST, current, 1, dustOptions);
        }
    }

    @Override
    public String getId() {
        return "chainedsword";
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
        return "§6Chained Sword";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§cHitting an enemy pulls all nearby",
            "§centities toward the impact point and",
            "§cmakes hostile mobs target the victim.",
            "§cApplies the victim's on-hit effects to",
            "§cpulled enemies and creates chain effects.",
            "",
            "§cOn Hit",
            "§9Summon",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }

    @Override
    public Material getMaterial() {
        return Material.DIAMOND_SWORD;
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

