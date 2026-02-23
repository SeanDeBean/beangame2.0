package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.Random;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.registry.BeangameItemRegistry;
import com.beangamecore.items.type.BGConsumableI;
import com.beangamecore.items.type.BeangameSoftItem;
import com.beangamecore.util.Cooldowns;
import com.beangamecore.util.ItemNBT;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.beangamecore.Main;

public class WitchsBrew extends BeangameItem implements BGConsumableI {
    
    private static final Random WITCHS_BREW_RANDOMIZER = new Random();

    private static final PotionEffectType[] WITCHS_BREW_POSITIVE_EFFECTS = {
        PotionEffectType.ABSORPTION,
        PotionEffectType.CONDUIT_POWER,
        PotionEffectType.DOLPHINS_GRACE,
        PotionEffectType.FIRE_RESISTANCE,
        PotionEffectType.GLOWING,
        PotionEffectType.HASTE,
        PotionEffectType.HEALTH_BOOST,
        PotionEffectType.HERO_OF_THE_VILLAGE,
        PotionEffectType.INSTANT_HEALTH,
        PotionEffectType.INVISIBILITY,
        PotionEffectType.JUMP_BOOST,
        PotionEffectType.LUCK,
        PotionEffectType.NIGHT_VISION,
        PotionEffectType.REGENERATION,
        PotionEffectType.RESISTANCE,
        PotionEffectType.SATURATION,
        PotionEffectType.SLOW_FALLING,
        PotionEffectType.SPEED,
        PotionEffectType.STRENGTH,
        PotionEffectType.WATER_BREATHING
    };

    private static final PotionEffectType[] WITCHS_BREW_NEGATIVE_EFFECTS = {
        PotionEffectType.BAD_OMEN,
        PotionEffectType.BLINDNESS,
        PotionEffectType.DARKNESS,
        PotionEffectType.HUNGER,
        PotionEffectType.MINING_FATIGUE,
        PotionEffectType.NAUSEA,
        PotionEffectType.POISON,
        PotionEffectType.RAID_OMEN, // custom in some versions
        PotionEffectType.SLOWNESS,
        PotionEffectType.UNLUCK,
        PotionEffectType.WEAKNESS,
        PotionEffectType.WITHER
    };

    private static final String[] BEANGAME_POSTIVE_EFFECTS = {
        "schizophrenic", "jumbling"
    };

    private static final String[] BEANGAME_NEGATIVE_EFFECTS = {
        "silenced", "use_item", "slot_enforced", "immobilized"
    };

    private void witchsbrewEffect(Player player) {
        int maxItems = Bukkit.getOnlinePlayers().stream()
                .mapToInt(this::countItems)
                .max()
                .orElse(0); // fallback to 0 if no players online (shouldn't happen)
        int currentItemCount = countItems(player);

        boolean onlyPositive = currentItemCount <= (maxItems - 3);
        int duration = 12;

        if (onlyPositive) {
            applyPositiveEffect(player, duration);
        } else {
            applyRandomEffect(player, duration);
        }
    }

    private void applyPositiveEffect(Player player, int duration) {
        // Pick a positive potion or Beangame effect
        boolean giveBeangamePositive = Math.random() < BEANGAME_POSTIVE_EFFECTS.length
                / (WITCHS_BREW_POSITIVE_EFFECTS.length + BEANGAME_POSTIVE_EFFECTS.length);

        if (giveBeangamePositive) {
            applyBeangamePositiveEffect(player, duration);
        } else {
            applyPositivePotionEffect(player, duration);
        }
    }

    private void applyBeangamePositiveEffect(Player player, int duration) {
        String type = BEANGAME_POSTIVE_EFFECTS[WITCHS_BREW_RANDOMIZER.nextInt(BEANGAME_POSTIVE_EFFECTS.length)];
        Cooldowns.setCooldown(type, player.getUniqueId(), 1000 * duration);
    }

    private void applyPositivePotionEffect(Player player, int duration) {
        PotionEffectType type = WITCHS_BREW_POSITIVE_EFFECTS[WITCHS_BREW_RANDOMIZER
                .nextInt(WITCHS_BREW_POSITIVE_EFFECTS.length)];
        applyPotionEffect(player, type, duration);
    }

    private void applyRandomEffect(Player player, int duration) {
        // Pool of all effects
        int total = WITCHS_BREW_POSITIVE_EFFECTS.length + WITCHS_BREW_NEGATIVE_EFFECTS.length
                + BEANGAME_POSTIVE_EFFECTS.length + BEANGAME_NEGATIVE_EFFECTS.length;
        int index = WITCHS_BREW_RANDOMIZER.nextInt(total);

        if (index < WITCHS_BREW_POSITIVE_EFFECTS.length) {
            PotionEffectType type = WITCHS_BREW_POSITIVE_EFFECTS[index];
            applyPotionEffect(player, type, duration);
        } else if (index < WITCHS_BREW_POSITIVE_EFFECTS.length + WITCHS_BREW_NEGATIVE_EFFECTS.length) {
            int offset = index - WITCHS_BREW_POSITIVE_EFFECTS.length;
            PotionEffectType type = WITCHS_BREW_NEGATIVE_EFFECTS[offset];
            player.addPotionEffect(new PotionEffect(type, duration * 20, 0));
        } else if (index < WITCHS_BREW_POSITIVE_EFFECTS.length + WITCHS_BREW_NEGATIVE_EFFECTS.length
                + BEANGAME_POSTIVE_EFFECTS.length) {
            int offset = index - WITCHS_BREW_POSITIVE_EFFECTS.length - WITCHS_BREW_NEGATIVE_EFFECTS.length;
            String type = BEANGAME_POSTIVE_EFFECTS[offset];
            Cooldowns.setCooldown(type, player.getUniqueId(), 1000 * duration);
        } else {
            int offset = index - WITCHS_BREW_POSITIVE_EFFECTS.length - WITCHS_BREW_NEGATIVE_EFFECTS.length
                    - BEANGAME_POSTIVE_EFFECTS.length;
            String type = BEANGAME_NEGATIVE_EFFECTS[offset];
            Cooldowns.setCooldown(type, player.getUniqueId(), 1000 * duration);
        }
    }

    private void applyPotionEffect(Player player, PotionEffectType type, int duration) {
        if (isInstantEffect(type)) {
            player.addPotionEffect(new PotionEffect(type, 1, 1));
        } else {
            player.addPotionEffect(new PotionEffect(type, duration * 20, 0));
        }
    }

    private boolean isInstantEffect(PotionEffectType type) {
        return type == PotionEffectType.INSTANT_HEALTH || type == PotionEffectType.ABSORPTION
                || type == PotionEffectType.SATURATION;
    }

    private Integer countItems(Player player) {
        Integer count = 0;
        for(ItemStack item : player.getInventory().getContents()){
            if(item == null){
                continue;
            }
            if(ItemNBT.hasBeanGameTag(item) && !(BeangameItemRegistry.getRaw(ItemNBT.getBeanGame(item)) instanceof BeangameSoftItem)){
                count++;
            }
        }
        return count;
    }

    @Override
    public void onConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem().clone();

        // Check if the consumed item is in the off-hand and set it again after a short delay
        if (item.equals(player.getEquipment().getItemInOffHand())) {
            scheduleTask(() -> player.getEquipment().setItemInOffHand(item));
        }

        // Check if the consumed item is in the main inventory and update it after a short delay
        for (int i = 0; i < 9; i++) {
            if (item.equals(player.getInventory().getItem(i))) {
                final int index = i;
                scheduleTask(() -> player.getInventory().setItem(index, item));
            }
        }

        // Apply Witch's Brew effect to the player
        witchsbrewEffect(player);

        // Set the main hand item amount to 0 after consumption
        player.getEquipment().getItemInMainHand().setAmount(0);
    }

    private void scheduleTask(Runnable task) {
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), task, 1);
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "witchsbrew";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "SBS", "SWS", "SCS", r.mCFromMaterial(Material.SUSPICIOUS_STEW), r.eCFromBeangame(Key.bg("bean")), r.mCFromMaterial(Material.WATER_BUCKET), r.mCFromMaterial(Material.CAULDRON));
        return null;
    }

    @Override
    public String getName() {
        return "§2Witch's Brew";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§2Gives a random potion or beangame effect",
            "§2May be positive or negative depending on",
            "§2your current standing in the game.",
            "",
            "§2Food",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.MUSHROOM_STEW;
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

