package com.beangamecore.items;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.damage.entity.BGDReceiverHeldI;
import com.beangamecore.items.type.talisman.BGHeldTalismanI;
import com.beangamecore.util.BlockCategories;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BouncyShield extends BeangameItem implements BGDReceiverHeldI, BGHeldTalismanI {
    
    @Override
    public void applyHeldTalismanEffects(Player player, ItemStack item) {
        UUID uuid = player.getUniqueId();
        if(onCooldown(uuid) && getRemainingCooldown(uuid) % 1000 <= 50){
            // Get a random nearby block and set it to slime
            Location playerLoc = player.getLocation();
            World world = playerLoc.getWorld();
            
            boolean slimePlaced = false;
            Location target = null;
            
            // Try up to 10 times to find a valid spot
            for (int attempts = 0; attempts < 10; attempts++) {
                // Generate random offset (within 5 blocks of player)
                int offsetX = (int) (Math.random() * 11) - 5; // -5 to 5
                int offsetY = (int) (Math.random() * 5) - 2;  // -2 to 2
                int offsetZ = (int) (Math.random() * 11) - 5; // -5 to 5
                
                Location blockLoc = playerLoc.clone().add(offsetX, offsetY, offsetZ);
                Block targetBlock = world.getBlockAt(blockLoc);
                
                // Only replace if it's not air (so we don't place slime in empty space)
                if (targetBlock.getType() != Material.AIR && !BlockCategories.getFunctionalBlocks().contains(targetBlock.getType())) {
                    targetBlock.setType(Material.SLIME_BLOCK);
                    target = targetBlock.getLocation();
                    slimePlaced = true;
                    break; // Found a valid spot, stop trying
                }
            }
            
            // Play slime sound if we successfully placed a block
            if (slimePlaced) {
                world.playSound(target, Sound.BLOCK_SLIME_BLOCK_PLACE, 0.87f, 1.0f);
            }
        } 
    }

    @Override
    public void victimOnHit(EntityDamageByEntityEvent event, ItemStack item) {
        if(event.getFinalDamage() == 0){
            if (onCooldown(event.getEntity().getUniqueId())){
                return;
            }
            applyCooldown(event.getEntity().getUniqueId());
            event.getEntity().getWorld().playSound(event.getEntity().getLocation(), Sound.ITEM_SHIELD_BLOCK, 0.5F, 1);
            event.getEntity().getWorld().playSound(event.getEntity().getLocation(), Sound.ENTITY_WIND_CHARGE_WIND_BURST, 1, 0.5F);
            Location l1 = event.getEntity().getLocation();
            Location l2 = event.getDamager().getLocation();
            Vector v = l1.subtract(l2).toVector().normalize().multiply(-1.8);
            boolean hasKBResistance = false;
            if(event.getDamager() instanceof Player victim){
                hasKBResistance = victim.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE) != null && 
                        victim.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE).getValue() >= 0.7;
            }
            if (!hasKBResistance) {
                event.getDamager().setVelocity(event.getDamager().getVelocity().add(v));
            }
        }
    }
    
    @Override
    public long getBaseCooldown() {
        return 5000;
    }

    @Override
    public String getId() {
        return "bouncyshield";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "CBC", "SHS", " C ", r.mCFromMaterial(Material.WIND_CHARGE), r.eCFromBeangame(Key.bg("bean")), r.mCFromMaterial(Material.SLIME_BLOCK), r.mCFromMaterial(Material.SHIELD));
        return null;
    }

    @Override
    public String getName() {
        return ChatColor.DARK_AQUA + "Bouncy Shield";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§bWhen blocking damage, knocks attackers",
            "§bback powerfully. While held, randomly",
            "§bconverts nearby blocks to slime blocks",
            "§bwhen on cooldown.",
            "",
            "§bHeld Talisman",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }

    @Override
    public Material getMaterial() {
        return Material.SHIELD;
    }

    @Override
    public int getCustomModelData() {
        return 0;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_UNBREAKABLE);
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

