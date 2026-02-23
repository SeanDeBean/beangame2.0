package com.beangamecore.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGToolI;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

import com.beangamecore.Main;

import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class Treecapitator extends BeangameItem implements BGToolI {

    @Override
    public void onBlockBreak(BlockBreakEvent event, ItemStack item) {
        Block block = event.getBlock();
        String type = block.getType().toString();
        Player player = event.getPlayer();
        if(type.endsWith("_LOG") && !player.isSneaking()){
            List<Block> logs = getLogs(block.getLocation(), type);
            logs.remove(block);
            if(hasLeaves(logs, type.substring(0, type.length()-4) + "_LEAVES")){
                int i = 0;
                World world = block.getWorld();
                for(Block log : logs){
                    i++;
                    Location loc = log.getLocation();
                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable(){
                        public void run(){
                            log.breakNaturally();
                            world.playSound(loc, Sound.BLOCK_WOOD_BREAK, 1, 1);
                        }
                    }, 2L * i);
                }
            }
        }
    }

    private List<Block> getLogs(Location center, String type){
        List<Block> logs = new ArrayList<>();
        List<Block> next = getSurrounding(center, type);
        while(!next.isEmpty()){
            List<Block> nextBlock = new ArrayList<>();
            for(Block log : next){
                if(!logs.contains(log)){
                    logs.add(log);
                    nextBlock.addAll(getSurrounding(log.getLocation(), type));
                }
            }
            next = nextBlock;
        }
        return logs;
    }

    private List<Block> getSurrounding(Location center, String type) {
        List<Block> logs = new ArrayList<>();
        for (int x = -1; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                for (int z = -1; z < 2; z++) {
                    processLocation(center, type, logs, x, y, z);
                }
            }
        }
        return logs;
    }

    private void processLocation(Location center, String type, List<Block> logs, int x, int y, int z) {
        Location loc = center.clone().add(x, y, z);
        Block block = loc.getBlock();
        // Check if location is not the center and block type ends with given type
        if (!center.equals(loc) && block.getType().name().endsWith(type)) {
            logs.add(block);
        }
    }

    private boolean hasLeaves(List<Block> logs, String type){
        for(Block log : logs){
            if(!getSurrounding(log.getLocation(), type).isEmpty()){
                return true;
            }
        }
        return false;
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "treecapitator";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "NC ", "CP ", " E ", r.mCFromMaterial(Material.NETHERITE_INGOT), r.eCFromBeangame(Key.bg("cosmicingot")), r.eCFromBeangame(Key.bg("plaxe")), r.eCFromBeangame(Key.bg("extractinator")));
        return null;
    }

    @Override
    public String getName() {
        return "§5Treecapitator";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§5Breaks entire trees at once when",
            "§5chopping the bottom log.",
            "§5Sneak to break single blocks.",
            "",
            "§5Tool",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }

    @Override
    public Material getMaterial() {
        return Material.GOLDEN_AXE;
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

