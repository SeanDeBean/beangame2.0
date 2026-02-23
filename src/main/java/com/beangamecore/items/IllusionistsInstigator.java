package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.util.Cooldowns;

import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import com.beangamecore.Main;

public class IllusionistsInstigator extends BeangameItem implements BGRClickableI {
    
    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        event.setCancelled(true);
        // cooldown
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (onCooldown(uuid)) {
            sendCooldownMessage(player);
            return false;
        }
        applyCooldown(uuid);
        // item event
        Location loc = player.getLocation();
        World world = loc.getWorld();
        for (LivingEntity illusionistsinstigatorvictim : world.getLivingEntities()) {
            if (isValidVictim(illusionistsinstigatorvictim, world, loc, uuid)) {
                spawnCloneAndScheduleEffects(player, illusionistsinstigatorvictim,
                        illusionistsinstigatorvictim.getLocation(), world);
            }
        }
        return true;
    }

    private boolean isValidVictim(LivingEntity victim, World world, Location loc, UUID playerUUID) {
        // Check if victim is in the same world, within 20 blocks, not the player, and
        // in survival mode if a player
        if (!(victim.getLocation().getWorld().equals(world))) {
            return false;
        }
        if (victim.getLocation().distance(loc) >= 20.0D) {
            return false;
        }
        if (victim.getUniqueId().equals(playerUUID)) {
            return false;
        }
        if (victim instanceof Player && !((Player) victim).getGameMode().equals(GameMode.SURVIVAL)) {
            return false;
        }
        return true;
    }

    private void spawnCloneAndScheduleEffects(Player player, LivingEntity illusionistsinstigatorvictim, Location vloc,
            World world) {
        double nang = (vloc.getYaw() + 90.0F);
        if (nang < 0.0D) {
            nang += 360.0D;
        }
        double nX = Math.cos(Math.toRadians(nang));
        double nZ = Math.sin(Math.toRadians(nang));
        Location cloneLocation = new Location(world, vloc.getX() - nX, vloc.getY(), vloc.getZ() - nZ, vloc.getYaw(),
                vloc.getPitch());
        Zombie clone = world.spawn(cloneLocation, Zombie.class);
        clone.setAdult();
        clone.setCustomName(player.getCustomName());
        clone.setAI(false);
        clone.setInvulnerable(true);
        clone.getEquipment().setArmorContents(player.getEquipment().getArmorContents());
        clone.getEquipment().setItemInMainHand(player.getEquipment().getItemInMainHand());

        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skullMeta.setOwningPlayer(player);
        skull.setItemMeta(skullMeta);
        clone.getEquipment().setHelmet(skull);

        world.playSound(cloneLocation, Sound.ENTITY_ENDERMAN_TELEPORT, 0.5F, 0);
        world.spawnParticle(Particle.END_ROD, cloneLocation, 50, 0.5, 0.5, 0.5, 0.1);

        // delayed hit
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {
            public void run() {
                illusionistsinstigatorvictim.damage(4, player);
                Cooldowns.setCooldown("slot_enforced", illusionistsinstigatorvictim.getUniqueId(), 1200L);
                clone.swingMainHand();
            }
        }, 8L);

        // delayed despawn
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {
            public void run() {
                world.playSound(cloneLocation, Sound.ENTITY_ENDERMAN_DEATH, 0.5F, 0);
                world.spawnParticle(Particle.EXPLOSION, cloneLocation, 1, 0, 0, 0, 0);
                clone.getEquipment().clear();
                clone.remove();
            }
        }, 15L);
    }
    
    @Override
    public long getBaseCooldown() {
        return 24000L;
    }

    @Override
    public String getId() {
        return "illusionistsinstigator";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "  I", " C ", "S  ", r.eCFromBeangame(Key.bg("illagerwannabe")), r.mCFromMaterial(Material.SOUL_CAMPFIRE), r.mCFromMaterial(Material.STICK));
        return null;
    }

    @Override
    public String getName() {
        return "§3Illusionist's Instigator";
    }

    @Override
public List<String> getLore() {
    return List.of(
        "§dCreates clones of yourself next to",
        "§dnearby enemies that deal 2 hearts",
        "§dof damage and apply on-hit effects.",
        "",
        "§dOn Hit Extender",
        "§9Castable",
        "§9§obeangame"
    );
}

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }

    @Override
    public Material getMaterial() {
        return Material.GOLDEN_SHOVEL;
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

