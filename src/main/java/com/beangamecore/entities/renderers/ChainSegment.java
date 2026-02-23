package com.beangamecore.entities.renderers;

import org.bukkit.util.Vector;

public class ChainSegment {
    private Vector position;
    private double length;

    public ChainSegment(Vector position, double length) {
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
