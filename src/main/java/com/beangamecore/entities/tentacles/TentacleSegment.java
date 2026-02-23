package com.beangamecore.entities.tentacles;

import org.bukkit.util.Vector;

class TentacleSegment {
    private Vector position;
    private double length;
    
    public TentacleSegment(Vector position, double length) {
        this.position = position;
        this.length = length;
    }
    
    public Vector getPosition() {
        return position;
    }
    
    public void setPosition(Vector position) {
        this.position = position;
    }
    
    public double getLength() {
        return length;
    }
}
