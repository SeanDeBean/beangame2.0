package com.beangamecore.entities.renderers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.World;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.util.Vector;

public class KinematicChainVisualizer {
    private Vector root;
    private List<ChainSegment> segments;
    private int iterator = 0;
    private int prevIterator = 0;
    private int stage = 0;
    private Vector target;

    public KinematicChainVisualizer(Vector root, List<ChainSegment> segments) {
        this.root = root;
        this.segments = segments;
        reset();
    }

    public void resetIterator() {
        iterator = segments.size() - 1;
        stage = 0;
    }

    public void reset() {
        resetIterator();
        target = null;

        Vector direction = new Vector(0, 1, 0);
        Vector rotAxis = new Vector(1, 0, -1);
        double rotation = -0.2;
        Vector pos = root.clone();
        for (ChainSegment segment : segments) {
            direction.rotateAroundAxis(rotAxis, rotation);
            pos.add(direction.clone().multiply(segment.getLength()));
            segment.getPosition().copy(pos);
        }
    }

    public void step() {
        if (target == null) return;

        prevIterator = iterator;

        if (stage == 0) {
            ChainSegment previousSegment = (iterator + 1 < segments.size()) ? segments.get(iterator + 1) : null;
            ChainSegment segment = segments.get(iterator);

            if (previousSegment == null) {
                segment.getPosition().copy(target);
            } else {
                fabrikMoveSegment(segment.getPosition(), previousSegment.getPosition(), previousSegment.getLength());
            }

            if (iterator == 0) {
                stage = 1;
            } else {
                iterator--;
            }
        } else {
            Vector previousSegmentPos = (iterator - 1 >= 0) ? segments.get(iterator - 1).getPosition() : root;
            ChainSegment segment = segments.get(iterator);

            fabrikMoveSegment(segment.getPosition(), previousSegmentPos, segment.getLength());

            if (iterator == segments.size() - 1) {
                stage = 0;
            } else {
                iterator++;
            }
        }
    }

    public void straighten(Vector target) {
        resetIterator();

        Vector direction = target.clone().subtract(root).normalize();
        direction.setY(0.5);
        direction.normalize();

        Vector position = root.clone();
        for (ChainSegment segment : segments) {
            position.add(direction.clone().multiply(segment.getLength()));
            segment.getPosition().copy(position);
        }
    }

    private void fabrikMoveSegment(Vector point, Vector pullTowards, double segmentLength) {
        Vector direction = pullTowards.clone().subtract(point).normalize();
        point.copy(pullTowards).subtract(direction.multiply(segmentLength));
    }

    public void render(World world, boolean renderAll) {
        Vector rootLocation = this.root;

        Vector direction = this.segments.get(this.segments.size() - 1).getPosition().clone().subtract(rootLocation);
        Vector upVector = direction.clone().crossProduct(new Vector(0, 1, 0));

        for (int i = 0; i < this.segments.size(); i++) {
            ChainSegment segment = this.segments.get(i);
            boolean needsUpdate = renderAll || i == this.prevIterator;

            if (needsUpdate) {
                float thickness = (this.segments.size() - i) * 1.5f / 16f;

                Vector vector = segment.getPosition().clone().subtract(rootLocation);
                if (!vector.isZero()) vector.normalize().multiply(segment.getLength());

                Vector pos = segment.getPosition().clone().subtract(vector.clone());

                BlockDisplay display = BlockDisplayRenderer.renderSegment(pos.toLocation(world), segment, thickness, upVector);
                display.setInterpolationDuration(4);
                display.setTeleportDuration(4);
                display.setBrightness(new Display.Brightness(0, 15));
            }

            rootLocation = segment.getPosition();
        }
    }

    public void unRender(BlockDisplayRenderer renderer) {
        for (ChainSegment segment : this.segments) {
            renderer.clear(BlockDisplayRenderer.Identifier.chainSegment(segment));
        }
    }

    public static KinematicChainVisualizer create(int segments, double length, Vector root) {
        List<ChainSegment> segmentList = new ArrayList<>();
        for (int i = 0; i < segments; i++) {
            segmentList.add(new ChainSegment(root.clone(), length));
        }
        return new KinematicChainVisualizer(root.clone(), segmentList);
    }

    public Vector getTarget(){
        return target;
    }
}
