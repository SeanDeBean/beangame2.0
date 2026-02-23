package com.beangamecore.entities.rat;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Silverfish;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.profile.PlayerProfile;

import com.beangamecore.registry.BeangameItemRegistry;

public class CheeseTouchRat {
    
    private Silverfish base;
    private ItemDisplay rat;
    private int ticksAlive;
    private static ItemStack skull = null;

    public static CopyOnWriteArrayList<CheeseTouchRat> rats = new CopyOnWriteArrayList<>();
    
    private static void createSkull() {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta headMeta = (SkullMeta) head.getItemMeta();

        PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID(), null);
        URL url = null;
        try {
            URI uri = new URI("http://textures.minecraft.net/texture/b8f3f81ee9d08146c7b6a532308db2f55da2b816c6df83ffe1952152f086a5ab");
            url = uri.toURL(); // Convert URI to URL
        } catch (URISyntaxException | MalformedURLException e) {
            e.printStackTrace();
            return; // Exit if the URL or URI is malformed
        }
        profile.getTextures().setSkin(url);

        headMeta.setOwnerProfile(profile);

        head.setItemMeta(headMeta);
        skull = head;
    }

    public static void summon(Location loc, LivingEntity target, LivingEntity owner) {
        rats.add(new CheeseTouchRat(loc, target, owner));
    }

    public CheeseTouchRat(Location loc, LivingEntity target, LivingEntity owner) {
        World world = loc.getWorld();
        rat = (ItemDisplay) world.spawnEntity(loc, EntityType.ITEM_DISPLAY);
        if(skull == null){
            createSkull();
        }
        rat.setItemStack(skull);
        ticksAlive = 0;
        base = world.spawn(loc, Silverfish.class);
        base.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 9999, 1, false, false));
        base.setInvisible(true);
        base.setTarget(target);
        base.setCanPickupItems(true);
        base.setCustomName(owner.getName() + "'s rat");
        if(Math.random() < 0.0014){
            base.getEquipment().setItemInMainHand(BeangameItemRegistry.getRaw("cheesetouch").asItem());
            base.getEquipment().setItemInMainHandDropChance(0f);
        }
        rat.setTeleportDuration(1);
    }

    public void tickRats() {
        ticksAlive++;
        Location loc = base.getLocation().add(0, 0.5, 0);
        loc.add(loc.getDirection().normalize().multiply(-0.25));
        loc.setYaw(loc.getYaw() + 180);
        rat.teleport(loc); // Ensure the display is above the Silverfish
    }

    public int getTicksAlive() {
        return ticksAlive;
    }

    public Silverfish getSilverfish() {
        return base;
    }

    public ItemDisplay getDisplay() {
        return rat;
    }

    public void remove() {
        rat.getWorld().playSound(rat.getLocation(), Sound.ENTITY_BAT_AMBIENT, 1F, 1);
        rat.getWorld().spawnParticle(Particle.DRIPPING_HONEY, rat.getLocation(), 1);
        rat.remove();
        base.remove();
    }
}

