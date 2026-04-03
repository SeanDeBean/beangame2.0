package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.damage.entity.BGDDealerInvI;
import com.beangamecore.items.type.general.BGResetableI;
import com.beangamecore.items.type.voicechat.BGVCMicPacket;
import com.beangamecore.items.type.voicechat.BGVoicechat;
import com.beangamecore.recipes.RecipeAPI;
import com.beangamecore.util.Cooldowns;
import com.beangamecore.util.Key;
import com.beangamecore.util.Longs;

import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class AssassinsHitlist extends BeangameItem implements BGDDealerInvI, BGVoicechat, BGVCMicPacket, BGResetableI {

    VoicechatServerApi api;

    @Override
    public void onInitialize(VoicechatServerApi api) {
        this.api = api;
    }

    @Override
    public void resetItem(){
        Longs.register("spearofares_hits");
    }

    @Override
    public void onMicrophonePacket(MicrophonePacketEvent event) {
        if(event.getSenderConnection() != null){
            if (Cooldowns.onCooldown("silenced", event.getSenderConnection().getPlayer().getUuid())){
                event.cancel();
                Player p = Bukkit.getServer().getPlayer(event.getSenderConnection().getPlayer().getUuid());
                if(p != null){
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(ChatColor.WHITE + "You are silenced!"));
                }
                return;
            }
        }
    }
    
    @Override
    public void attackerInventoryOnHit(EntityDamageByEntityEvent event, ItemStack item) {
        LivingEntity user = (LivingEntity) event.getDamager();
        LivingEntity victim = (LivingEntity) event.getEntity();
        UUID uuid = user.getUniqueId();
        World world = victim.getWorld();

        if (user.equals(victim) || onCooldown(uuid)) {
            return;
        }
        applyCooldown(uuid);

        // Retrieve the hit count
        long hitCount = Longs.getLong("assassinshitlist_hits", uuid);

        if (hitCount == 2L) {
            activateEffect(user, victim, world);
            Longs.setLong("assassinshitlist_hits", uuid, 0);
        } else {
            hitCount++;
            if(user instanceof Player u){
                u.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§5" + hitCount + " stacks"));
            }
            Longs.setLong("assassinshitlist_hits", uuid, hitCount);
        }
    }

    private void activateEffect(LivingEntity user, LivingEntity victim, World world) {

        if(user instanceof Player u){
            u.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§5Activated"));
        }

        // Play sound and spawn particles
        Location userLocation = user.getLocation();
        Location victimLocation = victim.getLocation();
        world.playSound(userLocation, Sound.ENTITY_EVOKER_FANGS_ATTACK, 1.0F, 1.0F);
        world.spawnParticle(Particle.SONIC_BOOM, victimLocation, 2);

        // Play particle at new location
        world.spawnParticle(Particle.SONIC_BOOM, victimLocation, 2);

        // Calculate the teleport location
        double targetAngle = (victimLocation.getYaw() + 90.0F);
        if (targetAngle < 0.0D) {
            targetAngle += 360.0D;
        }
        double nX = Math.cos(Math.toRadians(targetAngle));
        double nZ = Math.sin(Math.toRadians(targetAngle));
        Location newLoc = new Location(world, victimLocation.getX() - nX, victimLocation.getY(), victimLocation.getZ() - nZ, victimLocation.getYaw(), victimLocation.getPitch());
        user.teleport(newLoc);

        // Apply silencing effect to victim
        if(victim instanceof Player){
            Cooldowns.setCooldown("silenced", victim.getUniqueId(), 2000L);
        }
    }
    
    @Override
    public long getBaseCooldown() {
        return 333;
    }

    @Override
    public String getId() {
        return "assassinshitlist";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        RecipeAPI r = Main.recipeAPI;
        return r.bgShapedRecipe(this, " E ", "MBM", " M ", r.mCFromMaterial(Material.ENDER_EYE), r.mCFromMaterial(Material.MAP), r.eCFromBeangame(Key.bg("bean")));
    }

    @Override
    public String getName() {
        return "§7Assassin's Hitlist";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§3Every 3rd hit silences the target's",
            "§3voice chat and teleports you behind",
            "§3them with a sonic boom effect.",
            "",
            "§cOn Hit",
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
        return Material.PAPER;
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
    public EquipmentSlotGroup getSlot(){
        return null;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

}

