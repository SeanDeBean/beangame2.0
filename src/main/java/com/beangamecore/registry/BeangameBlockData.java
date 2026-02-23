package com.beangamecore.registry;

import com.beangamecore.blocks.generic.BeangameBlock;
import com.beangamecore.util.Key;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.persistence.ListPersistentDataType;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class BeangameBlockData {

    public static void addBeangameBlock(BeangameBlock bgblock, Block mcblock) {
        PersistentDataContainer persistentDataContainer = mcblock.getChunk().getPersistentDataContainer();
        List<PersistentDataContainer> blocks = Optional.ofNullable(
            persistentDataContainer.get(Key.BLOCKS, ListPersistentDataType.LIST.dataContainers())
        ).map(ArrayList::new).orElseGet(ArrayList::new);
        PersistentDataContainer blockData = persistentDataContainer.getAdapterContext().newPersistentDataContainer();
        blockData.set(Key.ID, PersistentDataType.STRING, bgblock.getKey().toString());
        blockData.set(Key.POSITION, ListPersistentDataType.LIST.integers(), List.of(mcblock.getX(), mcblock.getY(), mcblock.getZ()));
        blockData.set(Key.MATERIAL, PersistentDataType.STRING, bgblock.getBlockType().name());
        blocks.add(blockData);
        persistentDataContainer.set(Key.BLOCKS, ListPersistentDataType.LIST.dataContainers(), blocks);
    }

    public static Map<Block, BeangameBlock> getLoadedBeangameBlocks(World world){
        Map<Block, BeangameBlock> blocks = new HashMap<>();
        for(Chunk chunk : world.getLoadedChunks()){
            blocks.putAll(getBeangameBlocks(chunk));
        }
        return blocks;
    }

    public static Map<Block, BeangameBlock> getBeangameBlocks(Chunk chunk){
        Map<Block, BeangameBlock> bgblocks = new HashMap<>();
        PersistentDataContainer persistentDataContainer = chunk.getPersistentDataContainer();
        List<PersistentDataContainer> blocks = persistentDataContainer.get(Key.BLOCKS, ListPersistentDataType.LIST.dataContainers());
        List<PersistentDataContainer> toRemove = new ArrayList<>();
        if(blocks == null) blocks = new ArrayList<>();
        else blocks = new ArrayList<>(blocks);
        for(PersistentDataContainer container : blocks){
            List<Integer> pos = container.get(Key.POSITION, ListPersistentDataType.LIST.integers());
            String type = container.get(Key.MATERIAL, PersistentDataType.STRING);
            if(pos == null || type == null) {
                toRemove.add(container);
                continue;
            }
            Block block = chunk.getWorld().getBlockAt(pos.get(0), pos.get(1), pos.get(2));
            Optional<BeangameBlock> bgb = getBeangameBlock(block);
            bgb.ifPresent(b -> bgblocks.put(block, b));
        }
        toRemove.forEach(blocks::remove);
        if(!toRemove.isEmpty()) persistentDataContainer.set(Key.BLOCKS, ListPersistentDataType.LIST.dataContainers(), blocks);
        return bgblocks;
    }

    public static Optional<BeangameBlock> getBeangameBlock(Block block){
        Optional<PersistentDataContainer> toRemove = Optional.empty();
        PersistentDataContainer persistentDataContainer = block.getChunk().getPersistentDataContainer();
        List<PersistentDataContainer> blocks = persistentDataContainer.get(Key.BLOCKS, ListPersistentDataType.LIST.dataContainers());
        if(blocks == null) blocks = new ArrayList<>();
        else blocks = new ArrayList<>(blocks);
        boolean loop = true;
        for(PersistentDataContainer container : blocks){
            if(!loop) continue;
            List<Integer> pos = container.get(Key.POSITION, ListPersistentDataType.LIST.integers());
            if(pos != null && pos.size() > 2){
                if(pos.get(0) == block.getX() && pos.get(1) == block.getY() && pos.get(2) == block.getZ()){
                    String id = container.get(Key.ID, PersistentDataType.STRING);
                    boolean isKey = id != null && id.contains(":");
                    String type = container.get(Key.MATERIAL, ListPersistentDataType.STRING);
                    if(type == null || id == null){
                        loop = false;
                        toRemove = Optional.of(container);
                    } else if(Material.getMaterial(type) != block.getType()){
                        loop = false;
                        Optional<BeangameBlock> bgb = isKey ? BeangameBlockRegistry.get(NamespacedKey.fromString(id)) : BeangameBlockRegistry.get(id);
                        bgb.ifPresent(beangameBlock -> beangameBlock.destroy(block));
                        toRemove = Optional.of(container);
                    } else return isKey ? BeangameBlockRegistry.get(NamespacedKey.fromString(id)) : BeangameBlockRegistry.get(id);
                }
            }
        }
        if(toRemove.isPresent()) {
            blocks.remove(toRemove.get());
            persistentDataContainer.set(Key.BLOCKS, ListPersistentDataType.LIST.dataContainers(), blocks);
        }
        return Optional.empty();
    }

    public static void removeBeangameBlock(Block block){
        PersistentDataContainer persistentDataContainer = block.getChunk().getPersistentDataContainer();
        List<PersistentDataContainer> blocks = persistentDataContainer.get(Key.BLOCKS, ListPersistentDataType.LIST.dataContainers());
        List<PersistentDataContainer> toRemove = new ArrayList<>();
        if(blocks == null) blocks = new ArrayList<>();
        else blocks = new ArrayList<>(blocks);
        for(PersistentDataContainer container : blocks){
            if(container.has(Key.POSITION) && container.has(Key.ID)){
                List<Integer> pos = container.get(Key.POSITION, ListPersistentDataType.LIST.integers());
                if(pos != null && pos.size() > 2){
                    if(pos.get(0) == block.getX() && pos.get(1) == block.getY() && pos.get(2) == block.getZ()){
                        toRemove.add(container);
                    }
                }
            }
        }
        toRemove.forEach(blocks::remove);
        persistentDataContainer.set(Key.BLOCKS, ListPersistentDataType.LIST.dataContainers(), blocks);
    }

    public static boolean isBeangameBlock(Block block){
        return getBeangameBlock(block) != null;
    }

}

