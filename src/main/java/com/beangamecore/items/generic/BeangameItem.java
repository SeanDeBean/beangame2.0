package com.beangamecore.items.generic;

import com.beangamecore.Main;
import com.beangamecore.commands.QuickCooldownCommand;
import com.beangamecore.registry.BeangameItemRegistry;
import com.beangamecore.util.Cooldowns;

import de.tr7zw.nbtapi.NBT;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.components.EquippableComponent;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class BeangameItem implements Keyed {

    Map<UUID, Long> cooldown = new HashMap<>();
    private final String namespace;

    public BeangameItem(){
        namespace = "beangame";
        Main.logger().info("Registered Beangame Item: ["+getKey()+"]");
        init();
    }

    protected void init(){

    }

    public String getNamespace() {
        return namespace;
    }

    public BeangameItem(String namespace){
        this.namespace = namespace;
        Main.logger().info("Registered Beangame Item: ["+getKey()+"]");
        init();
    }

    public abstract long getBaseCooldown();
    public abstract String getId();
    public abstract boolean isInItemRotation();
    public boolean isInFoodItemRotation(){
        return false;
    }

    public boolean inItemRotation(){
        return Main.getConfiguration().getItemMeta(this).inRotation();
    }

    public boolean inFoodItemRotation(){
        return Main.getConfiguration().getItemMeta(this).inFoodRotation();
    }

    public boolean onCooldown(UUID user){
        return cooldown.containsKey(user) && (cooldown.get(user) > System.currentTimeMillis());
    }

    public long getRemainingCooldown(UUID user){
        if(!cooldown.containsKey(user)) return 0;
        return cooldown.get(user) - System.currentTimeMillis();
    }

    public void resetCooldown(UUID player){
        cooldown.remove(player);
    }

    public void sendCooldownMessage(Player player){

        UUID uuid = player.getUniqueId();
        if (Cooldowns.onCooldown("redacted", uuid)){
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§8Cooldown redacted!"));
            return;
        }

        String color = getName().substring(0, 2);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(color + "Wait " + getRemainingCooldown(player.getUniqueId()) / 1000L + " second(s) before using again!"));
    }

    public NamespacedKey getKey(){
        return NamespacedKey.fromString(namespace+":"+getId());
    }

    public boolean is(Class<?> clazz){
        return clazz.isInstance(this);
    }

    public <T> void doIf(Class<T> clazz, Consumer<T> consumer){
        if(clazz.isInstance(this)) consumer.accept(clazz.cast(this));
    }

    public void doIfAll(Collection<Class<?>> clazzez, Consumer<BeangameItem> consumer){
        for(Class<?> clazz : clazzez){
            if(!clazz.isInstance(this)) return;
        }
        consumer.accept(this);
    }

    public <T> void doIf(Class<T> clazz, boolean condition, Consumer<T> consumer){
        if(clazz.isInstance(this) && condition) consumer.accept(clazz.cast(this));
    }

    public <T> void doIf(Class<T> clazz, Predicate<T> condition, Consumer<T> consumer){
        if(clazz.isInstance(this) && condition.test(clazz.cast(this))) consumer.accept(clazz.cast(this));
    }

    public <T> boolean getIf(Class<T> clazz, Predicate<T> predicate){
        if(clazz.isInstance(this) ) return predicate.test(clazz.cast(this));
        return false;
    }

    public <T> boolean getIf(Class<T> clazz, boolean condition, Predicate<T> predicate){
        if(clazz.isInstance(this) && condition) return predicate.test(clazz.cast(this));
        return false;
    }

    public <T> boolean getIf(Class<T> clazz, Predicate<T> condition, Predicate<T> predicate){
        if(clazz.isInstance(this) && condition.test(clazz.cast(this))) return predicate.test(clazz.cast(this));
        return false;
    }

    public void setCooldown(UUID user, long cd){
        cooldown.put(user, System.currentTimeMillis() + cd);
    }

    public void applyCooldown(UUID uuid) {
        Entity entity = Bukkit.getEntity(uuid);
        if (entity instanceof LivingEntity user) {
            long baseCooldown = getBaseCooldown();
            double cooldownMultiplier = 1.0;

            // Apply Haste: reduces cooldown
            if (user.hasPotionEffect(PotionEffectType.HASTE)) {
                PotionEffect haste = user.getPotionEffect(PotionEffectType.HASTE);
                int hasteLevel = haste.getAmplifier() + 1;

                // Haste: 25% reduction at level 1, approaches 60% as level → ∞
                double k = 0.2;
                double hasteReduction = 0.25 + 0.35 * (1 - Math.exp(-k * hasteLevel));
                cooldownMultiplier *= (1 - hasteReduction);
            }

            // Apply Mining Fatigue: increases cooldown
            if (user.hasPotionEffect(PotionEffectType.MINING_FATIGUE)) {
                PotionEffect fatigue = user.getPotionEffect(PotionEffectType.MINING_FATIGUE);
                int fatigueLevel = fatigue.getAmplifier() + 1;

                // Fatigue: 1.4x at level 1, approaches 2x as level → ∞
                double k = 0.3;
                double fatigueIncrease = 0.4 + 0.6 * (1 - Math.exp(-k * fatigueLevel));
                cooldownMultiplier *= (1 + fatigueIncrease);
            }

            long adjustedCooldown = (long) (baseCooldown * cooldownMultiplier);

            if(QuickCooldownCommand.getRandomizer()){
                adjustedCooldown *= 0.54;
            }

            cooldown.put(uuid, System.currentTimeMillis() + adjustedCooldown);
        }
    }

    public boolean canInteractWithMainHand(){
        return true;
    }

    public boolean canInteractWithOffHand(){
        return true;
    }

    public ItemStack asItem(){
        ItemStack stack = new ItemStack(getMaterial(), getCraftingAmount());
        Main.getConfiguration().applyMeta(stack, getKey());
        NBT.modify(stack, (nbt) -> {
            nbt.setString("beangame.itemkey", getKey().toString());
        });
        return stack;
    }

    public int getCraftingAmount(){
        return 1;
    }

    public abstract CraftingRecipe getCraftingRecipe();

    public RecipeChoice.ExactChoice asExactRecipeChoice(){
        return new RecipeChoice.ExactChoice(asItem());
    }

    public RecipeChoice.MaterialChoice asMaterialRecipeChoice(){
        return new RecipeChoice.MaterialChoice(getMaterial());
    }

    public abstract String getName();

    public abstract List<String> getLore();

    public abstract Map<String, Integer> getEnchantments();

    public abstract Material getMaterial();

    public abstract int getCustomModelData();

    public abstract List<ItemFlag> getItemFlags();

    public abstract ArmorTrim getArmorTrim();

    public abstract Color getColor();

    public abstract int getArmor();

    public abstract EquipmentSlotGroup getSlot();

    public EquippableComponent getEquipmentData(){
        return null;
    }

    public boolean isGlidingArmor(){
        return false;
    }

    public boolean isUnbreakable() {
        return getItemFlags().contains(ItemFlag.HIDE_UNBREAKABLE);
    }

    public abstract int getMaxStackSize();

    // cooldown hashmap cleanup
    static {
        Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
            for (BeangameItem item : BeangameItemRegistry.getRegistry().values()) {
                item.cleanupCooldowns();
            }
        }, 20 * 60L, 20 * 60L); // Runs every 60 seconds
    }

    public void cleanupCooldowns() {
        int before = cooldown.size();
        cooldown.keySet().removeIf(uuid -> {
            Entity entity = Bukkit.getEntity(uuid);
            return entity == null || (entity instanceof LivingEntity living && living.isDead());
        });
        // debug testing
        int after = cooldown.size();
        if (before != after) {
            Main.logger().info("Cleaned " + (before - after) + " cooldown entries for item [" + getKey() + "]");
        }
    }
    
}

