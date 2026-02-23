package com.beangamecore.entities.livingtree;

import org.bukkit.World;
import org.bukkit.entity.Player;

public class PlayerLaunchData {
    private final Player player;
    private final World originalWorld;
    private boolean hasLeftGround;
    
    public PlayerLaunchData(Player player) {
        this.player = player;
        this.originalWorld = player.getWorld();
        this.hasLeftGround = false;
    }
    
    // Getters and setters
    public Player getPlayer() { return player; }
    public World getOriginalWorld() { return originalWorld; }
    public boolean hasLeftGround() { return hasLeftGround; }
    public void setHasLeftGround(boolean hasLeftGround) { this.hasLeftGround = hasLeftGround; }
}
