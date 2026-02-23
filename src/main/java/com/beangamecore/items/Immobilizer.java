package com.beangamecore.items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.util.Cooldowns;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
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
import com.beangamecore.particles.BeangameParticleManager;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class Immobilizer extends BeangameItem implements BGRClickableI{
    
    private final Map<UUID, Long> immobilizershortcd = new HashMap<>();
    
    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        event.setCancelled(true);
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (checkCooldowns(player, uuid)) {
            return false;
        }

        World world = player.getWorld();
        Location loc = player.getEyeLocation();
        Bukkit.getScheduler().runTask(Main.getPlugin(), () -> {
            performRayTrace(player, world, loc);
        });
        return true;
    }

    private boolean checkCooldowns(Player player, UUID uuid) {
        // cooldown system
        if (onCooldown(uuid)) {
            sendCooldownMessage(player);
            return true;
        } else if (immobilizershortcd.containsKey(uuid) && immobilizershortcd.get(uuid) > System.currentTimeMillis()) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    TextComponent.fromLegacy("§eWait before using again!"));
            return true;
        }
        immobilizershortcd.put(uuid, System.currentTimeMillis() + 250L);
        return false;
    }

    private void performRayTrace(Player player, World world, Location loc) {
        RayTraceResult res = world.rayTrace(loc, loc.getDirection(), 24D, FluidCollisionMode.NEVER, true, 0.25,
                entity -> entity instanceof Player &&
                        !entity.getUniqueId().equals(player.getUniqueId()) &&
                        ((Player) entity).getGameMode() != GameMode.SPECTATOR);

        if (res == null || res.getHitEntity() == null) {
            return;
        } else if (res.getHitEntity().getType() == EntityType.PLAYER) {
            handlePlayerHit(player, world, loc, (Player) res.getHitEntity());
        }
    }

    private void handlePlayerHit(Player player, World world, Location loc, Player target) {
        applyPotionEffects(player, target);
        Location loc2 = target.getEyeLocation();

        createParticleTrail(loc, loc2, world);
        playSounds(world, loc, loc2);

        applyCooldown(player.getUniqueId());
        setImmobilizedCooldowns(player.getUniqueId(), target.getUniqueId());
    }

    private void applyPotionEffects(Player player, Player target) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 0));
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 1));
        target.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 1));
    }

    private void createParticleTrail(Location loc, Location loc2, World world) {
        BeangameParticleManager particleManager = Main.getPlugin().getParticleManager();
        AtomicInteger j = new AtomicInteger(0);

        for (int i = 0; i <= 10; i++) {
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {
                public void run() {
                    particleManager.particleTrail(loc, loc2, 255, 255, 0);
                    if (j.get() == 10) {
                        world.playSound(loc, Sound.BLOCK_IRON_DOOR_CLOSE, 1, 0);
                        world.playSound(loc2, Sound.BLOCK_IRON_DOOR_CLOSE, 1, 0);
                    }
                    j.set(j.get() + 1);
                }
            }, i * 10L);
        }
    }

    private void playSounds(World world, Location loc, Location loc2) {
        world.playSound(loc, Sound.BLOCK_IRON_DOOR_OPEN, 1, 0);
        world.playSound(loc2, Sound.BLOCK_IRON_DOOR_OPEN, 1, 0);
    }

    private void setImmobilizedCooldowns(UUID playerUuid, UUID targetUuid) {
        Cooldowns.setCooldown("immobilized", playerUuid, 5000L);
        Cooldowns.setCooldown("immobilized", targetUuid, 5000L);
    }

    @Override
    public long getBaseCooldown() {
        return 35000L;
    }

    @Override
    public String getId() {
        return "immobilizer";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "SSS", "SBS", "SSS", r.mCFromMaterial(Material.SLIME_BALL), r.eCFromBeangame(Key.bg("bean")));
        return null;
    }

    @Override
    public String getName() {
        return "§eImmobilizer";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to shoot a ray that",
            "§9immobilizes both you and the target",
            "§9for 5 seconds. Grants resistance to",
            "§9both you and your target.",
            "",
            "§aSupport",
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
        return Material.SLIME_BALL;
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
    public EquipmentSlotGroup getSlot(){
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

}

