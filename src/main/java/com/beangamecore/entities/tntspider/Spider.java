package com.beangamecore.entities.tntspider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import com.beangamecore.entities.renderers.BlockDisplayRenderer;
import com.beangamecore.entities.renderers.ChainSegment;
import com.beangamecore.entities.renderers.KinematicChain;
import com.beangamecore.entities.renderers.RenderDebugOptions;
import com.beangamecore.entities.renderers.Utils;

public class Spider {

    public static CopyOnWriteArrayList<Spider> spiders = new CopyOnWriteArrayList<>();

    private Location location;
    public LivingEntity owner;
    public int ticksAlive;
    private Gait gait;
    private SpiderBehaviour behaviour = new StayStillBehaviour();

    private BlockDisplayRenderer renderer = new BlockDisplayRenderer();

    private boolean updateFrontPairNext = true;

    public BlockDisplayRenderer getRenderer(){
        return renderer;
    }

    private Vector velocity = new Vector(0.0, 0.0, 0.0);

    private Leg leftFrontLeg;
    private Leg rightFrontLeg;
    private Leg leftBackLeg;
    private Leg rightBackLeg;
    private List<Leg> legs;

    private boolean isRotating = false;
    private boolean didHitGround = false;

    public Spider(Location location, Gait gait, LivingEntity owner) {

        this.location = location;
        this.gait = gait;
        this.owner = owner;

        // this.target = location.add(location.getDirection().normalize().multiply(100));

        this.ticksAlive = 0;

        this.leftFrontLeg = createLeg(new Vector(0.9, -gait.getBodyHeight(), 0.9), 0.9 * gait.getLegSegmentLength(), gait.getLegSegmentCount());
        this.rightFrontLeg = createLeg(new Vector(-0.9, -gait.getBodyHeight(), 0.9), 0.9 * gait.getLegSegmentLength(), gait.getLegSegmentCount());
        this.leftBackLeg = createLeg(new Vector(1.0, -gait.getBodyHeight(), -1.1), 1.2 * gait.getLegSegmentLength(), gait.getLegSegmentCount());
        this.rightBackLeg = createLeg(new Vector(-1.0, -gait.getBodyHeight(), -1.1), 1.2 * gait.getLegSegmentLength(), gait.getLegSegmentCount());
        this.legs = Arrays.asList(leftFrontLeg, rightFrontLeg, leftBackLeg, rightBackLeg);

        this.location.setY(location.getY() + gait.getBodyHeight());

        spiders.add(this);

        renderer.renderSpider(this, RenderDebugOptions.none());

        this.behaviour = new DirectionBehaviour(location.getDirection());
    }

    public void accelerateToVelocity(Vector targetVelocity) {
        Vector target = targetVelocity.clone();
        if (legs.stream().anyMatch(Leg::isUncomfortable)) {
            target.multiply(0);
        }
        Utils.lerpVectorByConstant(velocity, target.setY(velocity.getY()), gait.getWalkAcceleration());
    }

    public void rotateTowards(Vector targetDirection) {
        location.setYaw(location.getYaw() % 360);
        double oldYaw = Math.toRadians(location.getYaw());

        double targetYaw = Math.atan2(-targetDirection.getX(), targetDirection.getZ());

        double optimizedTargetYaw = Math.abs(targetYaw - oldYaw) > Math.PI ? 
            (targetYaw > oldYaw ? targetYaw - Math.PI * 2 : targetYaw + Math.PI * 2) : targetYaw;

        isRotating = Math.abs(optimizedTargetYaw - oldYaw) > 0.0001;

        if (!isRotating || legs.stream().anyMatch(Leg::isUncomfortable)) return;

        double newYaw = Utils.lerpNumberByConstant(oldYaw, optimizedTargetYaw, gait.getRotateSpeed());
        location.setYaw((float) Math.toDegrees(newYaw));

        // Rotate legs end effector
        for (Leg leg : legs) {
            if (leg.isGrounded()) continue;
            Vector vector = leg.getEndEffector().clone().subtract(location.toVector());
            vector.rotateAroundY(newYaw - oldYaw);
            leg.getEndEffector().copy(location.toVector()).add(vector);
        }
    }

    public void teleport(Location newLocation) {
        Vector diff = newLocation.toVector().subtract(location.toVector());

        location.setWorld(newLocation.getWorld());
        location.setX(newLocation.getX());
        location.setY(newLocation.getY());
        location.setZ(newLocation.getZ());

        for (Leg leg : legs) {
            leg.getEndEffector().add(diff);
            for (ChainSegment segment : leg.getChain().getSegments()) {
                segment.getPosition().add(diff);
            }
        }
    }

    public void update() {
        // Update behaviour
        didHitGround = false;
        isRotating = false;
        if (behaviour == null) {
            System.err.println("Spider has null behaviour (ticksAlive = " + ticksAlive + ")");
            return;
        }
        behaviour.update(this);

        // Apply gravity and air resistance
        velocity.setY(velocity.getY() - gait.getGravityAcceleration());
        velocity.setY(velocity.getY() * (1 - gait.getAirDragCoefficient()));

        if (isGrounded()) {
            // Adjust body height
            double legsAverageY = legs.stream().mapToDouble(leg -> leg.getTargetPosition().getY()).average().orElse(0);
            double targetY = legsAverageY + gait.getBodyHeight();
            double stabilizedY = Utils.lerpNumberByFactor(location.getY(), targetY, gait.getBodyHeightCorrectionFactor());
            double maxThrust = gait.getBodyHeightCorrectionAcceleration();
            double minThrust = 0.0;

            double thrust = Math.max(minThrust, Math.min(maxThrust, stabilizedY - location.getY() - velocity.getY()));
            velocity.setY(velocity.getY() + thrust);
        }

        // Resolve ground collision
        Vector bounce = new Vector(0.0, 0.0, 0.0);
        double resolveY = Utils.resolveGroundCollision(location.clone().add(velocity));
        if (resolveY > 0.0) {
            location.setY(location.getY() + resolveY);
            if (velocity.getY() < 0) bounce.setY(-velocity.getY() * gait.getBounceFactor());

            didHitGround = resolveY > (gait.getGravityAcceleration() * 2) * (1 - gait.getAirDragCoefficient());
        }

        // Apply velocity
        location.add(velocity);
        velocity.add(bounce);

        // update a pair of adjacent legs, toggling back and fourth between the two pairs
        // Decide which pair to update
        if (updateFrontPairNext) {
            // Update front legs
            leftFrontLeg.update();
            rightFrontLeg.update();
        } else {
            // Update back legs
            leftBackLeg.update();
            rightBackLeg.update();
        }

        // Toggle for next update call
        updateFrontPairNext = !updateFrontPairNext;
    }

    public List<Leg> adjacentLegs(Leg leg) {
        if (leg == leftFrontLeg) return Arrays.asList(rightFrontLeg, leftBackLeg);
        if (leg == rightFrontLeg) return Arrays.asList(rightBackLeg, leftFrontLeg);
        if (leg == leftBackLeg) return Arrays.asList(rightBackLeg, leftFrontLeg);
        if (leg == rightBackLeg) return Arrays.asList(rightFrontLeg, leftBackLeg);
        return Collections.emptyList();
    }

    public boolean isGrounded() {
        return (leftFrontLeg.isGrounded() && rightBackLeg.isGrounded()) || 
               (rightFrontLeg.isGrounded() && leftBackLeg.isGrounded());
    }

    private Leg createLeg(Vector restPosition, double segmentLength, int segmentsCount) {
        List<ChainSegment> segments = new ArrayList<>();
        for (int i = 1; i <= segmentsCount; i++) {
            Vector position = location.toVector().add(restPosition.clone().normalize().multiply(segmentLength * i));
            segments.add(new ChainSegment(position, segmentLength));
        }
        KinematicChain chain = new KinematicChain(location.toVector(), segments);
        return new Leg(this, restPosition, chain);
    }

    public Gait getGait(){
        return gait;
    }

    public boolean isRotating(){
        return isRotating;
    }

    public Vector getVelocity(){
        return velocity;
    }

    public Location getLocation(){
        return location;
    }

    public List<Leg> getLegs(){
        return legs;
    }

    public boolean didHitGround(){
        return didHitGround;
    }
}
