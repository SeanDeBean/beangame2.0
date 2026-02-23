package com.beangamecore.blocks.type;

import com.beangamecore.blocks.generic.BeangameBlock;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public interface BGBreakableB extends BeangameBlock {
    //Beangame Breakable Block
    void onBlockBreak(BlockBreakEvent event, ItemStack item);
}

