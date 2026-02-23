package com.beangamecore.entities.renderers;

import java.util.List;

import org.bukkit.util.Vector;

public class KinematicChain {
    
    private Vector root;
    private List<ChainSegment> segments;

    public KinematicChain(Vector root, List<ChainSegment> segments) {
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

    public void straighten(Vector target, double height) {
        Vector direction = target.clone().subtract(root).normalize();
        direction.setY(direction.getY() + height);
        direction.normalize();

        Vector position = root.clone();
        for (ChainSegment segment : segments) {
            position.add(direction.clone().multiply(segment.getLength()));
            segment.setPosition(position.clone());
        }
    }

    private void fabrikForward(Vector newPosition) {
        ChainSegment lastSegment = segments.get(segments.size() - 1);
        lastSegment.setPosition(newPosition.clone());

        for (int i = segments.size() - 1; i > 0; i--) {
            ChainSegment previousSegment = segments.get(i);
            ChainSegment segment = segments.get(i - 1);

            moveSegment(segment.getPosition(), previousSegment.getPosition(), previousSegment.getLength());
        }
    }

    private void fabrikBackward() {
        moveSegment(segments.get(0).getPosition(), root, segments.get(0).getLength());

        for (int i = 1; i < segments.size(); i++) {
            ChainSegment previousSegment = segments.get(i - 1);
            ChainSegment segment = segments.get(i);

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

    public List<ChainSegment> getSegments(){
        return segments;
    }

    public Vector getRoot(){
        return root;
    }
}
