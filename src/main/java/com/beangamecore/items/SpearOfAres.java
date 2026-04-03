package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.damage.entity.BGDDealerHeldI;
import com.beangamecore.items.type.general.BGCyclingI;
import com.beangamecore.items.type.general.BGResetableI;
import com.beangamecore.util.Longs;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class SpearOfAres extends BeangameItem implements BGDDealerHeldI, BGCyclingI, BGResetableI {

    @Override
    public void resetItem(){
        Longs.register("spearofares_hits");
    }

    private static int TOGGLE_DURATION = 14; // seconds
    private static boolean ACTIVE = false;
    private static BukkitTask task;

    private static final Sound ACTIVATE_SOUND = Sound.ENTITY_ENDER_DRAGON_GROWL;
    private static final Sound DEACTIVATE_SOUND = Sound.ENTITY_ILLUSIONER_MIRROR_MOVE;
    private static final Sound HIT_SOUND = Sound.ITEM_TOTEM_USE;
    private static final Sound EFFECT_TRIGGER_SOUND = Sound.ENTITY_WITHER_SHOOT;
    private static final float SOUND_VOLUME = 0.5f;

    @Override
    public void startCycle(){
        task = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
            ACTIVE = !ACTIVE;
            String message = ACTIVE ? 
                ChatColor.GOLD + "The Spear of Ares surges with power!" : 
                ChatColor.GRAY + "The spear's power wanes...";

            Sound sound = ACTIVE ? ACTIVATE_SOUND : DEACTIVATE_SOUND;
            float pitch = ACTIVE ? 0.8f : 1.2f;
            
            // Notify players holding the sword
            Bukkit.getOnlinePlayers().forEach(player -> {
                if (player.getInventory().getItemInMainHand() != null && this.asItem().isSimilar(player.getInventory().getItemInMainHand())) {

                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(message));
                    player.playSound(player.getLocation(), sound, SOUND_VOLUME, pitch);
                }
            });
        }, 0L, TOGGLE_DURATION * 20);
    }

    public void disableStateCycleTask(){
        if (task != null) task.cancel();
    }

    @Override
    public void attackerOnHit(EntityDamageByEntityEvent event, ItemStack item) {
        if(!ACTIVE) return;


        Player attacker = (Player) event.getDamager();
        UUID uuid = attacker.getUniqueId();

        if(onCooldown(uuid)){
            return;
        }
        applyCooldown(uuid);

        long hitCount = Longs.getLong("spearofares_hits", uuid);
        if (hitCount == 4L) {
            activateEffect((LivingEntity) event.getEntity());
            Longs.setLong("spearofares_hits", uuid, 0);
        } else {
            hitCount++;
            attacker.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§c" + hitCount + " stacks"));
            Longs.setLong("spearofares_hits", uuid, hitCount);
        }

        if (Math.random() < 0.08) {
            event.setDamage(event.getDamage() * 1.3);
            attacker.playSound(attacker.getLocation(), HIT_SOUND, SOUND_VOLUME, 1.0f);
        }
    }

    private void activateEffect(LivingEntity target){
        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 5 * 20, 0));
        target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 5 * 20, 0));

        spawnParticles(target, Particle.SOUL_FIRE_FLAME);
        target.getWorld().playSound(target.getLocation(), EFFECT_TRIGGER_SOUND, SOUND_VOLUME, 0.8f);
    }

    private void spawnParticles(LivingEntity target, Particle particle) {
        target.getWorld().spawnParticle(
            particle,
            target.getLocation().add(0, 1, 0),
            30,
            0.5, 0.5, 0.5,
            particle == Particle.DUST ? 
                new Particle.DustOptions(Color.RED, 1.0f) : 
                null
        );
    }
    
    @Override
    public long getBaseCooldown() {
        return 333;
    }

    @Override
    public String getId() {
        return "spearofares";
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
        return "§cSpear of Ares";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§cCycles between active and inactive states",
            "§cevery " + TOGGLE_DURATION + " seconds.",
            "§cWhen active: Every 5th hit applies",
            "§cwither and weakness for 5 seconds",
            "§c8% chance to deal 30% bonus damage.",
            "",
            "§cOn Hit",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }

    @Override
    public Material getMaterial() {
        return Material.IRON_SWORD;
    }

    @Override
    public int getCustomModelData() {
        return 105;
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
