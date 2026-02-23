package com.beangamecore.items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;

import com.beangamecore.Main;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class Swapper extends BeangameItem implements BGRClickableI {
    
    private static Map<UUID, Long> swappershortcd = new HashMap<>();

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        event.setCancelled(true);
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (handleCooldowns(player, uuid)) {
            return false;
        }
        swappershortcd.put(uuid, System.currentTimeMillis() + 250L);
        handleItemEvent(player, uuid);
        return true;
    }

    private boolean handleCooldowns(Player player, UUID uuid) {
        if (onCooldown(uuid)) {
            sendCooldownMessage(player);
            return true;
        } else if (swappershortcd.containsKey(uuid) && swappershortcd.get(uuid) > System.currentTimeMillis()) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    TextComponent.fromLegacy("§aWait before using again!"));
            return true;
        }
        return false;
    }

    private void handleItemEvent(Player player, UUID uuid) {
        World world = player.getWorld();
        Location loc = player.getEyeLocation();
        Bukkit.getScheduler().runTask(Main.getPlugin(), () -> {
            RayTraceResult res = performRayTrace(world, loc, player);
            if (res != null && res.getHitEntity() instanceof LivingEntity target) {
                swapLocationsAndEffects(player, target, world, uuid);
            }
        });
    }

    private RayTraceResult performRayTrace(World world, Location loc, Player player) {
        return world.rayTrace(
                loc,
                loc.getDirection(),
                48D,
                FluidCollisionMode.NEVER,
                true,
                0.25,
                entity -> entity instanceof LivingEntity &&
                        !(entity instanceof Player p && p.getGameMode() == GameMode.SPECTATOR) &&
                        !entity.getUniqueId().equals(player.getUniqueId()));
    }

    private void swapLocationsAndEffects(Player player, LivingEntity target, World world, UUID uuid) {
        Location loc1 = player.getLocation();
        Location loc2 = target.getLocation();

        Main.getPlugin().getParticleManager().particleTrail(player.getEyeLocation(), target.getEyeLocation(), 23, 245,
                20);
        world.playSound(loc1, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 0);
        world.playSound(loc2, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 0);
        world.spawnParticle(Particle.SPORE_BLOSSOM_AIR, loc1, 10);
        world.spawnParticle(Particle.SPORE_BLOSSOM_AIR, loc2, 10);

        if (target instanceof Player targetPlayer) {
            targetPlayer.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    TextComponent.fromLegacy("§aSwapped locations with " + player.getDisplayName() + "!"));
        }
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacy("§aSwapped locations with " + target.getName() + "!"));

        target.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 100, 0));

        player.teleport(loc2);
        target.teleport(loc1);
        applyCooldown(uuid);
    }

    @Override
    public long getBaseCooldown() {
        return 30000L;
    }

    @Override
    public String getId() {
        return "swapper";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "   ", "HEH", "   ", r.eCFromBeangame(Key.bg("hook")), r.mCFromMaterial(Material.ENDER_EYE));
        return null;
    }

    @Override
    public String getName() {
        return "§bSwapper";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to swap locations with",
            "§9the targeted entity up to 48 blocks away.",
            "§9Grants Absorption to the target.",
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
        return Material.BLAZE_ROD;
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

