package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGProjectileI;
import com.beangamecore.items.type.BGRClickableI;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.util.Vector;

import com.beangamecore.Main;

public class NecroticScythe extends BeangameItem implements BGRClickableI, BGProjectileI {
    
    @Override
    public void onProjHit(ProjectileHitEvent event) {
        event.setCancelled(true);
        event.getEntity().remove();
        Location loc = event.getEntity().getLocation();
        World world = loc.getWorld();
        world.playSound(loc, Sound.ENTITY_ZOMBIE_INFECT, 1.0F, 1.0F);
        if(event.getEntity().getShooter() instanceof Player){
            Player shooter = (Player) event.getEntity().getShooter();
            Entity necroticscythehusk = world.spawnEntity(loc, EntityType.HUSK);
            necroticscythehusk.setCustomName(shooter.getName() + "'s husk");
            if(((LivingEntity) necroticscythehusk).getEquipment().getItemInMainHand() == null || ((LivingEntity) necroticscythehusk).getEquipment().getItemInMainHand().getType().equals(Material.AIR)){
                ((LivingEntity) necroticscythehusk).getEquipment().setItemInMainHand(new ItemStack(Material.STONE_SWORD));
            }
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable(){
                public void run(){
                    if (necroticscythehusk.isValid() && !necroticscythehusk.isDead()) {
                        necroticscythehusk.remove();
                    }
                }
            }, 400L);
        }
    }

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        // cooldown system
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (onCooldown(uuid)){
            sendCooldownMessage(player);
            return false;
        }
        applyCooldown(uuid);
        // item event
        Location loc = player.getLocation();
        loc.setY(loc.getY()+1);
        player.getWorld().playSound(loc, Sound.ENTITY_EVOKER_PREPARE_SUMMON, 1.0F, 1.0F);
        for(int i = 0; i < 7; i++){
            Snowball snowball = launchProjectile(this, player, Snowball.class);
            Random rand = new Random();
            snowball.setVelocity(loc.getDirection().multiply(1.55).add(new Vector(rand.nextDouble(), 0, rand.nextDouble()).subtract(new Vector(rand.nextDouble(), 0, rand.nextDouble()))));
        }
        return true;
    }
    
    @Override
    public long getBaseCooldown() {
        return 32000L;
    }

    @Override
    public String getId() {
        return "necroticscythe";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, " CD", " B ", "S  ", r.eCFromBeangame(Key.bg("cosmicingot")), r.mCFromMaterial(Material.DIAMOND), r.eCFromBeangame(Key.bg("bean")), r.eCFromBeangame(Key.bg("spawncore")));
        return null;
    }

    @Override
    public String getName() {
        return "§4Necrotic Scythe";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to fire 7 projectiles",
            "§9that spawn husks on impact. Husks",
            "§9are armed with stone swords and",
            "§9despawn after 20 seconds.",
            "",
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
        return Material.NETHERITE_HOE;
    }

    @Override
    public int getCustomModelData() {
        return 101;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
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

