package com.beangamecore.items;

import com.beangamecore.Main;
import com.beangamecore.events.ServerLoad;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.items.type.damage.BGLateDamageInvI;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import java.util.*;

public class Rebirth extends BeangameItem implements BGRClickableI, BGLateDamageInvI {

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        rebirthReadCooldown(event.getPlayer());
        return true;
    }

    @Override
    public boolean onLateDamageInventory(EntityDamageEvent event, ItemStack item) {
        Player player = (Player) event.getEntity();
        if(SpoonsChance.hasActivatedSpoonsChance(player)){
            return false;
        }
        if(event.getFinalDamage() > player.getHealth() + player.getAbsorptionAmount()){
            if(event.isCancelled()) return false;
            UUID uuid = player.getUniqueId();
            if (onCooldown(uuid)){
                return false;
            }
            applyCooldown(uuid);
            event.setCancelled(true);
            rebirthSpawnEgg(player, event.getDamageSource());
        }
        return true;
    }

    public void rebirthReadCooldown(Player player){
        UUID uuid = player.getUniqueId();
        if(onCooldown(uuid)){
            sendCooldownMessage(player);
            return;
        }
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§3This item is off cooldown!"));
    }

    private void rebirthSpawnEgg(Player player, DamageSource damageSource) {
        UUID uuid = player.getUniqueId();
        World world = player.getWorld();
        Location loc = player.getLocation();
        loc.setX(Math.floor(loc.getX()));
        loc.setY(Math.floor(loc.getY() + 0.5));
        loc.setZ(Math.floor(loc.getZ()));
        world.playSound(loc, Sound.ITEM_MACE_SMASH_GROUND_HEAVY, 1, 0);
        for (int i = 0; i <= 32; i++) {
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {
                public void run() {
                    world.spawnParticle(Particle.INSTANT_EFFECT, loc, 3);
                }
            }, i * 5L);
        }
        if(!Revive.noRevive.contains(uuid)) Revive.noRevive.add(uuid);
        player.setGameMode(GameMode.SPECTATOR);

        // zombie in the ice
        Zombie clone = spawnPlayerClone(player, loc, world);

        List<Location> rebirthBlocks = new ArrayList<>();

        // actual ice
        for (int i = 0; i <= 2; i++) {
            rebirthBlocks.add(loc.clone());
            loc.subtract(1, 0, 0);
            rebirthBlocks.add(loc.clone());
            loc.subtract(0, 0, 1);
            rebirthBlocks.add(loc.clone());
            loc.add(1, 0, 0);
            rebirthBlocks.add(loc.clone());
            loc.add(0, 1, 1);
        }

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
            placeIceBlocks(world, rebirthBlocks);
        }, 1L);

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
            boolean rebirth = false;
            for (Location locs : rebirthBlocks) {
                Block block = world.getBlockAt(locs);
                Material type = block.getType();
                if (type.equals(Material.PACKED_ICE) || type.equals(Material.BLUE_ICE)) {
                    rebirth = true;
                    block.setType(Material.AIR);
                }
            }
            Revive.noRevive.remove(uuid);
            if(clone.isValid()) clone.remove();
            player.teleport(clone, TeleportCause.SPECTATE);
            player.setGameMode(GameMode.SURVIVAL);
            final boolean bool = rebirth;
            Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
                if (bool) {
                    player.setHealth(player.getAttribute(Attribute.MAX_HEALTH).getValue());
                    player.setFoodLevel(20);
                } else {
                    player.damage(999, damageSource);
                }
            }, 1);
        }, 180L);
    }

    private void placeIceBlocks(World world, List<Location> rebirthBlocks) {
        for (Location locs : rebirthBlocks) {
            Material type = Material.PACKED_ICE;
            if (Math.random() > 0.9) {
                type = Material.BLUE_ICE;
            }
            Block block = world.getBlockAt(locs);
            if (canPlaceBlock(block, locs)) {
                block.setType(type);
            }
        }
    }

    private boolean canPlaceBlock(Block block, Location locs) {
        return block != null
                && (block.getType() == Material.AIR || block.getType() == Material.WATER)
                && !outsideBorder(locs);
    }

    private Zombie spawnPlayerClone(Player player, Location loc, World world) {
        Zombie clone = world.spawn(loc, Zombie.class);
        clone.setAdult();
        clone.setCustomName(player.getCustomName());
        clone.setAI(false);
        clone.setInvulnerable(true);
        clone.setCollidable(false);
        clone.setGravity(false);
        ServerLoad.noCollisions.addEntry(clone.getUniqueId().toString());
        clone.getEquipment().setArmorContents(player.getEquipment().getArmorContents());
        clone.getEquipment().setItemInMainHand(player.getEquipment().getItemInMainHand());

        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skullMeta.setOwningPlayer(player);
        skull.setItemMeta(skullMeta);
        clone.getEquipment().setHelmet(skull);

        return clone;
    }

    private boolean outsideBorder(Location loc){
        WorldBorder border = loc.getWorld().getWorldBorder();
        double size = border.getSize() / 2;
        Location center = border.getCenter();
        double x = loc.getX() - center.getX();
        double z = loc.getZ() - center.getZ();
        return ((x > size || (-x) > size) || (z > size || (-z) > size));
    }

    @Override
    public long getBaseCooldown() {
        return 180000L;
    }

    @Override
    public String getId() {
        return "rebirth";
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
        return "§3Rebirth";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§3Prevents death once every 3 minutes",
            "§3by encasing you in an ice egg for",
            "§39 seconds. If ice blocks survive,",
            "§3you respawn with full health and",
            "§3hunger. Right-click to check cooldown.",
            "",
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
        return Material.TURTLE_EGG;
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
    public int getArmor(){
        return 0;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public EquipmentSlotGroup getSlot(){
        return null;
    }
}

