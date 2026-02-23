package com.beangamecore.entities.tntspider;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import com.beangamecore.entities.renderers.KinematicChain;
import com.beangamecore.entities.renderers.Utils;

public class Leg {
    private Spider parent;
    private Vector relativeRestPosition;
    private KinematicChain chain;

    private double scanGroundAbove = 2.0;
    private double scanGroundBelow = 2.0;

    private Vector restPosition;
    private Vector targetPosition;
    private Vector endEffector;

    private boolean uncomfortable = false;
    private boolean onGround = true;
    private boolean isMoving = false;
    private boolean isStranded = false;

    private boolean didStep = false;

    public Leg(Spider parent, Vector relativeRestPosition, KinematicChain chain) {
        this.parent = parent;
        this.relativeRestPosition = relativeRestPosition;
        this.chain = chain;
        this.restPosition = restPosition();

        Location ground = locateGround();
        this.targetPosition = ground != null ? ground.toVector() : restPosition.clone();
        this.endEffector = targetPosition.clone();
    }

    public double triggerDistance() {
        boolean isMoving = parent.getVelocity().getX() != 0.0 || parent.getVelocity().getZ() != 0.0;
        return !isMoving && !parent.isRotating() ? parent.getGait().getLegStationaryTriggerDistance() : parent.getGait().getLegMovingTriggerDistance();
    }

    public boolean isGrounded() {
        return onGround && !isMoving;
    }

    public void update() {
        updateMovement();

        chain.getRoot().copy(parent.getLocation().toVector());

        if (!parent.getGait().isLegNoStraighten()) {
            chain.straighten(endEffector, parent.getGait().getLegStraightenHeight());
        }
        chain.fabrik(endEffector);
    }

    private void updateMovement() {
        Gait gait = parent.getGait();

        didStep = false;

        restPosition = restPosition();

        Location ground = locateGround();
        targetPosition = ground != null ? ground.toVector() : restPosition.clone();
        isStranded = ground == null;

        double distanceToTarget = Utils.horizontalDistance(endEffector, targetPosition);
        uncomfortable = !isMoving && distanceToTarget > triggerDistance() && Utils.horizontalDistance(restPosition, endEffector) > gait.getLegDiscomfortDistance();
        onGround = onGround();

        // Inherit parent velocity
        if (!isGrounded()) {
            endEffector.add(parent.getVelocity());
        }

        // Resolve ground collision
        if (!onGround) {
            onGround = onGround();
            didStep = onGround;

            double yCollision = Utils.resolveGroundCollision(endEffector.toLocation(parent.getLocation().getWorld()));
            endEffector.setY(endEffector.getY() + yCollision);
        }

        if (isMoving) {
            // Move leg
            double moveSpeed = gait.getLegSpeed();

            Utils.lerpVectorByConstant(endEffector, targetPosition, moveSpeed);

            double liftHeight = gait.getLegLiftHeight();
            double groundY = targetPosition.getY();
            double hDistance = Utils.horizontalDistance(endEffector, targetPosition);

            if (hDistance > gait.getLegDropDistance()) {
                double liftedY = groundY + liftHeight;
                endEffector.setY(Utils.lerpNumberByConstant(endEffector.getY(), liftedY, moveSpeed));
            } else {
                endEffector.setY(Utils.lerpNumberByConstant(endEffector.getY(), groundY, moveSpeed));
            }

            if (endEffector.distance(targetPosition) < 0.0001) {
                isMoving = false;

                onGround = onGround();
                didStep = onGround;
            }

        } else {
            // Begin moving leg
            double verticalDistance = Utils.verticalDistance(endEffector, targetPosition);
            boolean canMove = isStranded || parent.adjacentLegs(this).stream().noneMatch(Leg::isMoving);
            if (canMove && (distanceToTarget > triggerDistance() || verticalDistance >= 0.0001)) {
                isMoving = true;
            }
        }
    }

    private boolean onGround() {
        return Utils.isOnGround(endEffector.toLocation(parent.getLocation().getWorld()));
    }

    public Vector restPosition() {
        return parent.getLocation().toVector().add(relativeRestPosition.clone().rotateAroundY(-Math.toRadians(parent.getLocation().getYaw())));
    }

    private Location locateGround() {
        Location location = restPosition.toLocation(parent.getLocation().getWorld());

        double x = restPosition.getX();
        double z = restPosition.getZ();

        Location mainCandidate = rayCast(x, z, location);

        if (!parent.getGait().isLegScanAlternativeGround()) return mainCandidate;

        if (mainCandidate != null) {
            if (mainCandidate.getY() >= location.getY() - 0.24 && mainCandidate.getY() <= location.getY() + 1.5) {
                return mainCandidate;
            }
        }

        double margin = 2 / 16.0;
        double nx = Math.floor(x) - margin;
        double nz = Math.floor(z) - margin;
        double pz = Math.ceil(z) + margin;
        double px = Math.ceil(x) + margin;

        List<Location> candidates = Arrays.asList(
            rayCast(nx, nz, location), rayCast(nx, z, location), rayCast(nx, pz, location),
            rayCast(x, nz, location), mainCandidate, rayCast(x, pz, location),
            rayCast(px, nz, location), rayCast(px, z, location), rayCast(px, pz, location)
        );

        Location preferredLocation = location.clone();

        Block frontBlock = location.clone().add(parent.getLocation().getDirection().clone().multiply(1)).getBlock();
        if (!frontBlock.isPassable()) {
            preferredLocation.setY(preferredLocation.getY() + parent.getGait().getLegScanHeightBias());
        }

        return candidates.stream()
            .filter(c -> c != null)
            .min(Comparator.comparingDouble(c -> c.distanceSquared(preferredLocation)))
            .orElse(null);
    }

    private Location rayCast(double x, double z, Location reference) {
        double y = reference.getY() + scanGroundAbove;
        Location startScan = new Location(reference.getWorld(), x, y, z);
        RayTraceResult hit = startScan.getWorld().rayTraceBlocks(
            startScan,
            new Vector(0.0, -1.0, 0.0),
            scanGroundAbove + scanGroundBelow,
            FluidCollisionMode.NEVER,
            true
        );
        return hit != null ? hit.getHitPosition().toLocation(startScan.getWorld()) : null;
    }

    public KinematicChain getChain(){
        return chain;
    }

    public Vector getTargetPosition(){
        return targetPosition;
    }

    public boolean didStep(){
        return didStep;
    }

    public Spider getParent(){
        return parent;
    }

    public boolean isUncomfortable(){
        return uncomfortable;
    }

    public Vector getEndEffector() {
        return endEffector;
    }

    public boolean isMoving(){
        return isMoving;
    }

    public double getScanGroundAbove(){
        return scanGroundAbove;
    }

    public double getScanGroundBelow(){
        return scanGroundBelow;
    }

    public boolean isStranded(){
        return isStranded;
    }

    public boolean isOnGround(){
        return onGround;
    }
}
