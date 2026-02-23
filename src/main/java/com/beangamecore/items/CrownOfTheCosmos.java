package com.beangamecore.items;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGArmorI;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

import java.util.*;

public class CrownOfTheCosmos extends BeangameItem implements BGArmorI {
    
    public static HashMap<UUID, List<CosmicCirclingStar>> stars = new HashMap<>();

    @Override
    public void applyArmorEffects(Player player, ItemStack item) {
        if(!stars.containsKey(player.getUniqueId())) stars.put(player.getUniqueId(), new ArrayList<>());
        List<CosmicCirclingStar> s = stars.get(player.getUniqueId());
        if(s.size() < 6 && player.getGameMode() != GameMode.SPECTATOR){
            s.add(new CosmicCirclingStar(player));
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ALLAY_ITEM_THROWN, 1, 0.5F);
        }
    }

    public static void onLeftClick(PlayerInteractEvent event, ItemStack stack) {
        if(event.getAction() == Action.LEFT_CLICK_AIR){
            List<CosmicCirclingStar> s = stars.get(event.getPlayer().getUniqueId());
            if(s != null && !s.isEmpty()){
                CosmicCirclingStar c = s.get(0);
                s.remove(c);
                c.remove();
                Location eye = event.getPlayer().getEyeLocation();
                Location dir = eye.clone().add(eye.getDirection());
                CosmicFallingStar.summon(eye, dir, event.getPlayer());
            }
        }
    }

    public static void tick(){
        stars.forEach((u, l) -> l.forEach(CosmicCirclingStar::tickFallingStars));
    }

    public void releaseStars(Player owner){
        UUID uuid = owner.getUniqueId();
        if(stars.containsKey(uuid)){
            for(CosmicCirclingStar star : stars.get(uuid)){
                star.remove();
            }
            stars.remove(uuid);
        }
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "crownofthecosmos";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "ICI", "IBI", "   ", r.eCFromBeangame(Key.bg("cosmicingot")), r.eCFromBeangame(Key.bg("crownofthegreedyking")), r.eCFromBeangame(Key.bg("bean")));
        return null;
    }

    @Override
    public String getName() {
        return "§5Crown of The Cosmos";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§6Generates cosmic stars that orbit around",
            "§6you while worn. Left-click to throw stars",
            "§6as projectiles. Can have up to 6 stars",
            "§6active at once.",
            "",
            "§6Armor",
            "§dOn Hit Extenders",
            "§9§obeangame",
            "§9", "§7When on Head:", "§9+2 Armor"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }

    @Override
    public Material getMaterial() {
        return Material.GOLDEN_HELMET;
    }

    @Override
    public int getCustomModelData() {
        return 0;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ARMOR_TRIM);
    }

    @Override
    public ArmorTrim getArmorTrim() {
        return new ArmorTrim(TrimMaterial.AMETHYST, TrimPattern.SILENCE);
    }

    @Override
    public Color getColor() {
        return null;
    }

    @Override
    public int getArmor() {
        return 2;
    }

    @Override
    public EquipmentSlotGroup getSlot(){
        return EquipmentSlotGroup.HEAD;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
    
}

