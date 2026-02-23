package com.beangamecore.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Illusioner;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import com.beangamecore.Main;

import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class IllagerWannabe extends BeangameItem implements BGRClickableI {
    
    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        // cooldown system
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (onCooldown(uuid)) {
            sendCooldownMessage(player);
            return false;
        }
        applyCooldown(uuid);

        handleItemEvent(player);

        return true;
    }

    private void handleItemEvent(Player player) {
        Location loc = player.getLocation();
        player.getWorld().playSound(loc, Sound.ENTITY_EVOKER_PREPARE_SUMMON, 1.0F, 1.0F);

        // Store references to the spawned illusioners
        List<Illusioner> spawnedIllusioners = spawnIllusioners(player, loc);

        scheduleTargetClearing(spawnedIllusioners);
        scheduleIllusionerCleanup(spawnedIllusioners);
    }

    private List<Illusioner> spawnIllusioners(Player player, Location loc) {
        List<Illusioner> spawnedIllusioners = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            Illusioner illusioner = spawnConfiguredIllusioner(player.getWorld(), loc, player);
            spawnedIllusioners.add(illusioner);
        }
        return spawnedIllusioners;
    }

    private void scheduleTargetClearing(List<Illusioner> spawnedIllusioners) {
        // Schedule target clearing for these specific illusioners after 1 second
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {
            public void run() {
                for (Illusioner illusioner : spawnedIllusioners) {
                    if (illusioner.isValid() && !illusioner.isDead()) {
                        illusioner.setTarget(null);
                    }
                }
            }
        }, 20L);
    }

    private void scheduleIllusionerCleanup(List<Illusioner> spawnedIllusioners) {
        // Schedule cleanup for these specific illusioners after 20 seconds
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {
            public void run() {
                for (Illusioner illusioner : spawnedIllusioners) {
                    if (illusioner.isValid() && !illusioner.isDead()) {
                        illusioner.setHealth(0);
                    }
                }
            }
        }, 400L);
    }

    private Illusioner spawnConfiguredIllusioner(World world, Location location, Player owner) {
        Illusioner illusioner = (Illusioner) world.spawnEntity(location, EntityType.ILLUSIONER);
        illusioner.setCustomName(owner.getName() + "'s illusioner");
        illusioner.setVelocity(location.getDirection().multiply(2.5));
        return illusioner;
    }

    @Override
    public long getBaseCooldown() {
        return 24000L;
    }

    @Override
    public String getId() {
        return "illagerwannabe";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "BE ", "ES ", "   ", r.eCFromBeangame(Key.bg("bean")), r.mCFromMaterial(Material.EMERALD_BLOCK), r.eCFromBeangame(Key.bg("spawncore")));
        return null;
    }

    @Override
    public String getName() {
        return "§2Illager Wannabe";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Summons two illusioners that launch",
            "§9forward and attack nearby enemies.",
            "§9They despawn after 20 seconds.",
            "",
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
        return Material.EMERALD;
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

