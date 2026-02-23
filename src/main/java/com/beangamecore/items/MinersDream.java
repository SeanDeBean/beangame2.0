package com.beangamecore.items;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGToolI;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class MinersDream extends BeangameItem implements BGToolI {
    
    @Override
    public void onBlockBreak(BlockBreakEvent event, ItemStack item) {
        Block block = event.getBlock();

        if(!oreDrops.containsKey(block.getType())){
            initializeOreDrops();
        }

        OreDrop drop = oreDrops.get(block.getType());
        
        if (drop != null) {
            event.setDropItems(false);
            dropItems(event.getPlayer().getWorld(), block.getLocation(), drop);
        }
    }

    private void dropItems(World world, Location location, OreDrop drop) {
        world.dropItemNaturally(location, new ItemStack(drop.getMaterial(), drop.getAmount()));
    }

    private final Map<Material, OreDrop> oreDrops = new HashMap<>();

    private void initializeOreDrops() {
        // Regular ores
        registerOre(Material.COAL_ORE, Material.COAL, 3);
        registerOre(Material.DEEPSLATE_COAL_ORE, Material.COAL, 3);
        registerOre(Material.DIAMOND_ORE, Material.DIAMOND, 3);
        registerOre(Material.DEEPSLATE_DIAMOND_ORE, Material.DIAMOND, 3);
        registerOre(Material.EMERALD_ORE, Material.EMERALD, 3);
        registerOre(Material.DEEPSLATE_EMERALD_ORE, Material.EMERALD, 3);
        registerOre(Material.IRON_ORE, Material.RAW_IRON, 3);
        registerOre(Material.DEEPSLATE_IRON_ORE, Material.RAW_IRON, 3);
        registerOre(Material.COPPER_ORE, Material.RAW_COPPER_BLOCK, 1);
        registerOre(Material.DEEPSLATE_COPPER_ORE, Material.RAW_COPPER_BLOCK, 1);
        registerOre(Material.GOLD_ORE, Material.RAW_GOLD, 3);
        registerOre(Material.DEEPSLATE_GOLD_ORE, Material.RAW_GOLD, 3);
        registerOre(Material.NETHER_GOLD_ORE, Material.RAW_GOLD, 3);
        registerOre(Material.NETHER_QUARTZ_ORE, Material.QUARTZ, 3);
        registerOre(Material.LAPIS_ORE, Material.LAPIS_BLOCK, 2);
        registerOre(Material.DEEPSLATE_LAPIS_ORE, Material.LAPIS_BLOCK, 2);
        registerOre(Material.REDSTONE_ORE, Material.REDSTONE_BLOCK, 1);
        registerOre(Material.DEEPSLATE_REDSTONE_ORE, Material.REDSTONE_BLOCK, 1);
        registerOre(Material.AMETHYST_CLUSTER, Material.AMETHYST_SHARD, 1);
    }

    private void registerOre(Material oreType, Material dropMaterial, int amount) {
        oreDrops.put(oreType, new OreDrop(dropMaterial, amount));
    }

    private static class OreDrop {
        private final Material material;
        private final int amount;

        public OreDrop(Material material, int amount) {
            this.material = Objects.requireNonNull(material);
            this.amount = validateAmount(amount);
        }

        private int validateAmount(int amount) {
            return Math.max(1, amount); // Ensure at least 1 item drops
        }

        public Material getMaterial() {
            return material;
        }

        public int getAmount() {
            return amount;
        }
    }
    
    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "minersdream";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "LTE", " C ", " B ", r.mCFromMaterial(Material.LAPIS_BLOCK), r.mCFromMaterial(Material.TURTLE_SCUTE), r.mCFromMaterial(Material.EMERALD_BLOCK), r.eCFromBeangame(Key.bg("hasteconverter")), r.eCFromBeangame(Key.bg("bean")));
        return null;
    }

    @Override
    public String getName() {
        return "§6Miner's Dream";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§5Mines ores with guaranteed multiplied",
            "§5drops equivalent to Fortune III.",
            "§5Drops processed materials like blocks",
            "§5and raw metal blocks directly.",
            "",
            "§5Tool",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:efficiency", 2, "minecraft:fortune", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.DIAMOND_PICKAXE;
    }

    @Override
    public int getCustomModelData() {
        return 102;
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

