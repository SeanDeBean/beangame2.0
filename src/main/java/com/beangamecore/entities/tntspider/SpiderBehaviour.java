package com.beangamecore.entities.tntspider;

import org.bukkit.Location;
import org.bukkit.util.Vector;

interface SpiderBehaviour {
    void update(Spider spider);
}

class StayStillBehaviour implements SpiderBehaviour {
    @Override
    public void update(Spider spider) {
        spider.accelerateToVelocity(new Vector(0.0, 0.0, 0.0));
    }
}

class TargetBehaviour implements SpiderBehaviour {
    private final Location target;
    private final double distance;

    public TargetBehaviour(Location target, double distance) {
        this.target = target;
        this.distance = distance;
    }

    @Override
    public void update(Spider spider) {
        Vector targetDirection = target.toVector().clone().subtract(spider.getLocation().toVector()).normalize();
        spider.rotateTowards(targetDirection);

        double currentSpeed = spider.getVelocity().length();
        double decelerateDistance = (currentSpeed * currentSpeed) / (2 * spider.getGait().getWalkAcceleration());
        double currentDistance = horizontalDistance(spider.getLocation(), target);

        if (currentDistance > distance + decelerateDistance) {
            spider.accelerateToVelocity(targetDirection.multiply(spider.getGait().getWalkSpeed()));
        } else {
            spider.accelerateToVelocity(new Vector(0.0, 0.0, 0.0));
        }
    }

    private double horizontalDistance(Location loc1, Location loc2) {
        return Math.sqrt(Math.pow(loc1.getX() - loc2.getX(), 2) + Math.pow(loc1.getZ() - loc2.getZ(), 2));
    }
}

class DirectionBehaviour implements SpiderBehaviour {
    private final Vector direction;

    public DirectionBehaviour(Vector direction) {
        this.direction = direction;
    }

    @Override
    public void update(Spider spider) {
        spider.rotateTowards(direction);
        spider.accelerateToVelocity(direction.clone().multiply(spider.getGait().getWalkSpeed()));
    }
}
