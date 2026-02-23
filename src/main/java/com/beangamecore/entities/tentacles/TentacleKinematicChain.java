package com.beangamecore.entities.tentacles;

import java.util.List;

import org.bukkit.util.Vector;

class TentacleKinematicChain {
    private Vector root;
    private List<TentacleSegment> segments;
    
    public TentacleKinematicChain(Vector root, List<TentacleSegment> segments) {
        this.root = root;
        this.segments = segments;
    }
    
    public void fabrik(Vector target) {
        double tolerance = 0.01;
        
        for (int i = 0; i < 10; i++) {
            fabrikForward(target);
            fabrikBackward();
            
            if (getEndEffector().distance(target) < tolerance) {
                break;
            }
        }
    }
    
    private void fabrikForward(Vector newPosition) {
        TentacleSegment lastSegment = segments.get(segments.size() - 1);
        lastSegment.setPosition(newPosition.clone());
        
        for (int i = segments.size() - 1; i > 0; i--) {
            TentacleSegment previousSegment = segments.get(i);
            TentacleSegment segment = segments.get(i - 1);
            
            moveSegment(segment.getPosition(), previousSegment.getPosition(), previousSegment.getLength());
        }
    }
    
    private void fabrikBackward() {
        moveSegment(segments.get(0).getPosition(), root, segments.get(0).getLength());
        
        for (int i = 1; i < segments.size(); i++) {
            TentacleSegment previousSegment = segments.get(i - 1);
            TentacleSegment segment = segments.get(i);
            
            moveSegment(segment.getPosition(), previousSegment.getPosition(), segment.getLength());
        }
    }
    
    private void moveSegment(Vector point, Vector pullTowards, double segmentLength) {
        Vector direction = pullTowards.clone().subtract(point).normalize();
        point.copy(pullTowards.clone().subtract(direction.multiply(segmentLength)));
    }
    
    public Vector getEndEffector() {
        return segments.get(segments.size() - 1).getPosition();
    }
    
    public List<TentacleSegment> getSegments() {
        return segments;
    }
    
    public Vector getRoot() {
        return root;
    }
}
