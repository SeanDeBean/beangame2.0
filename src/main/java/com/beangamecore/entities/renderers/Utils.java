package com.beangamecore.entities.renderers;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class Utils {
    public static double lerpNumberByFactor(double current, double target, double factor) {
        return current + (target - current) * factor;
    }

    public static double lerpNumberByConstant(double current, double target, double constant) {
        double distance = target - current;
        return Math.abs(distance) < constant ? target : current + constant * Math.signum(distance);
    }

    public static void lerpVectorByConstant(Vector current, Vector target, double constant) {
        Vector diff = target.clone().subtract(current);
        double distance = diff.length();
        if (distance <= constant) {
            current.copy(target);
        } else {
            current.add(diff.multiply(constant / distance));
        }
    }

    public static double verticalDistance(Vector a, Vector b) {
        return Math.abs(a.getY() - b.getY());
    }

    public static double horizontalDistance(Vector a, Vector b) {
        double x = a.getX() - b.getX();
        double z = a.getZ() - b.getZ();
        return Math.sqrt(x * x + z * z);
    }

    public static double horizontalDistance(Location a, Location b) {
        double x = a.getX() - b.getX();
        double z = a.getZ() - b.getZ();
        return Math.sqrt(x * x + z * z);
    }

    public static boolean isOnGround(Location location) {
        Location adjustedLocation = location.clone().add(0.0, -0.0001, 0.0);
        var block = adjustedLocation.getBlock();

        if (block.isPassable()) return false;

        var boundingBox = block.getBoundingBox();
        return boundingBox.contains(location.toVector());
    }

    public static double resolveGroundCollision(Location location) {
        var block = location.getBlock();
        if (block.isPassable()) return 0.0;
        var boundingBox = block.getBoundingBox();
        return boundingBox.contains(location.toVector()) ? boundingBox.getMaxY() - location.getY() : 0.0;
    }

    public static Vector crossProduct(Vector v1, Vector v2) {
        double x = v1.getY() * v2.getZ() - v1.getZ() * v2.getY();
        double y = v1.getZ() * v2.getX() - v1.getX() * v2.getZ();
        double z = v1.getX() * v2.getY() - v1.getY() * v2.getX();
        return new Vector(x, y, z);
    }
}
