package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.PufferFish;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.damage.entity.BGDReceiverInvI;
import com.beangamecore.items.type.talisman.BGLPTalismanI;

public class PufferfishPillar extends BeangameItem implements BGLPTalismanI, BGDReceiverInvI {

    @Override
    public void victimInventoryOnHit(EntityDamageByEntityEvent event, ItemStack itemStack){
        if(event.getDamager() instanceof PufferFish){
            event.setCancelled(true);
        }
    }

    @Override
    public void applyTalismanEffects(Player player, ItemStack item){
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
            summonPillar(player);
        }, new Random().nextInt(60));
    }

    private void summonPillar(Player player){
        Location randomLocation = getRandomLocation(player.getLocation(), 7);
        World world = player.getWorld();
        AtomicInteger j = new AtomicInteger(1);
        for(int i = 0; i < 5; i++){
            Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
                PufferFish pufferFish = (PufferFish) world.spawnEntity(randomLocation.clone().add(0, j.get(), 0), EntityType.PUFFERFISH);
                pufferFish.setAI(false);
                pufferFish.setPuffState(2);
                pufferFish.setInvulnerable(true);
                world.playSound(pufferFish.getLocation(), Sound.ENTITY_PLAYER_SPLASH, 0.5F, 1);
                AttributeInstance attribute = pufferFish.getAttribute(Attribute.SCALE);
                attribute.setBaseValue(attribute.getBaseValue() * 2);
                j.set(j.get() + 1);
                Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
                    if(pufferFish.isValid()){
                        world.playSound(pufferFish.getLocation(), Sound.ENTITY_GENERIC_SPLASH, 0.5F, 1);
                        pufferFish.remove();
                    }
                }, 80);
            }, i*3L);
        }
    }

    private Location getRandomLocation(Location location, int radius){
        Random random = new Random();
        double angle = random.nextDouble() * 2 * Math.PI;
        double xOffset = radius * Math.cos(angle);
        double zOffset = radius * Math.sin(angle);
        Location loc = location.clone().add(xOffset, 0, zOffset);
        loc.setY(loc.getWorld().getHighestBlockYAt(loc.getBlockX(), loc.getBlockZ()));
        return loc;
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "pufferfishpillar";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "PFP", "PDP", "PSP", r.mCFromMaterial(Material.PUFFERFISH), r.mCFromMaterial(Material.PUFFERFISH_BUCKET), r.eCFromBeangame(Key.bg("drunicedge")), r.eCFromBeangame(Key.bg("spawncore")));
        return null;
    }

    @Override
    public String getName() {
        return "§6Pufferfish Pillar";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§3Randomly spawns pillars of 5",
            "§3pufferfish within 7 blocks while held.",
            "§3Pufferfish are enlarged, fully puffed,",
            "§3and invulnerable. Grants immunity",
            "§3to damage from your own pufferfish.",
            "",
            "§3Talisman",
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
        return Material.PUFFERFISH;
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

