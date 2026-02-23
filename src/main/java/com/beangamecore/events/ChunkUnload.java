package com.beangamecore.events;

import com.beangamecore.blocks.generic.BeangameBlock;
import com.beangamecore.blocks.type.BGLoadableB;
import com.beangamecore.registry.BeangameBlockData;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.Map;

public class ChunkUnload implements Listener {
    @EventHandler
    void onUnload(ChunkUnloadEvent event){
        Map<Block, BeangameBlock> blocks = BeangameBlockData.getBeangameBlocks(event.getChunk());
        blocks.forEach((block, beangameBlock) -> {
            if(beangameBlock instanceof BGLoadableB l) l.onUnload(block);
        });
    }
}

