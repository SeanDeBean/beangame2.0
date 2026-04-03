package com.beangamecore.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.damage.entity.BGDDealerInvI;
import com.beangamecore.items.type.general.BG2tTickingI;
import com.beangamecore.util.Cooldowns;

public class TalismanOfJumbledFates extends BeangameItem implements BGDDealerInvI, BG2tTickingI {

    @Override
    public void attackerInventoryOnHit(EntityDamageByEntityEvent event, ItemStack item){
        LivingEntity entity = (LivingEntity) event.getEntity();
        if(entity instanceof Player){
            UUID uuid = entity.getUniqueId();
            Cooldowns.setCooldown("jumbling", uuid, 30000L);
        }
    }

    public void applyJumbling(Player player){
        double radius = 9.5;
        for(Player target : getTargets(player, radius)){
            scrambleInventory(target);
        }
        Cooldowns.setCooldown("jumbling", player.getUniqueId(), 0);
        DustOptions dustOptions = new DustOptions(Color.fromRGB(255, 0, 255), 2);
        Main.getPlugin().getParticleManager().spawnParticleSphere(player.getLocation(), radius, Particle.DUST, dustOptions, 250);
    }

    public static void scrambleInventory(Player player) {
        Inventory inventory = player.getInventory();

        // Get the player's inventory contents
        ItemStack[] contents = inventory.getContents();

        // Create a list to hold the items (excluding nulls)
        ArrayList<ItemStack> items = extractNonNullItems(contents);

        // Shuffle the items
        Collections.shuffle(items);

        // Clear the inventory
        inventory.clear();

        // Randomly assign items to slots
        assignItemsToRandomSlots(items, inventory);
    }

    private static ArrayList<ItemStack> extractNonNullItems(ItemStack[] contents) {
        ArrayList<ItemStack> items = new ArrayList<>();
        for (ItemStack item : contents) {
            if (item != null) {
                items.add(item);
            }
        }
        return items;
    }

    private static void assignItemsToRandomSlots(ArrayList<ItemStack> items, Inventory inventory) {
        Random random = new Random();
        HashSet<Integer> usedSlots = new HashSet<>();
        for (ItemStack item : items) {
            int slot;
            do {
                slot = random.nextInt(inventory.getSize());
            } while (usedSlots.contains(slot));
            usedSlots.add(slot);
            inventory.setItem(slot, item);
        }
    }

    private List<Player> getTargets(Player centerP, double radius) {
        Location center = centerP.getLocation();
        List<Player> targets = new ArrayList<>();
        for (Player potential : center.getWorld().getPlayers()) {
            if (isWithinRadius(potential, center, radius)) {
                if (!hasProtectionItem(potential)) {
                    targets.add(potential);
                }
            }
        }
        if (targets.contains(centerP)) {
            targets.remove(centerP);
        }
        return targets;
    }

    private boolean isWithinRadius(Player player, Location center, double radius) {
        return player.getLocation().distance(center) <= radius;
    }

    private boolean hasProtectionItem(Player player) {
        boolean protect = false;
        for (ItemStack item : player.getInventory().getContents()) {
            if (this.asItem().isSimilar(item)) {
                protect = true;
            }
        }
        return protect;
    }

    @Override
    public void tick(){
        for(Player player : Bukkit.getOnlinePlayers()){
            if(Cooldowns.onCooldown("jumbling", player.getUniqueId()) && player.getGameMode() != GameMode.SPECTATOR){
                DustOptions dustOptions = new DustOptions(Color.fromRGB(255, 0, 255), 2);
                player.getWorld().spawnParticle(Particle.DUST, player.getLocation().add(0, 0.5, 0), 1, dustOptions);
            }
        }
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "talismanofjumbledfates";
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
        return ChatColor.LIGHT_PURPLE + "Talisman of Jumbled Fates";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§3On hit applies jumbling effect to enemy",
            "§3players. On death, players with jumbling",
            "§3scramble the inventories of all nearby",
            "§3players. This grants the carrier immunity",
            "§3to the scrambling effects.",
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
        return Material.MAGENTA_DYE;
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
    public ArmorTrim getArmorTrim() {
        return null;
    }

    @Override
    public Color getColor() {
        return null;
    }

    @Override
    public int getArmor() {
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
