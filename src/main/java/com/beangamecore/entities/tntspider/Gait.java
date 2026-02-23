package com.beangamecore.entities.tntspider;

public class Gait {
    private double walkSpeed = 0.15;
    private double legSpeed = walkSpeed * 3;
    private double walkAcceleration = walkSpeed / 7;
    private double rotateSpeed = walkSpeed;
    private double legLiftHeight = 0.30;
    private double legDropDistance = legLiftHeight;
    private double legStationaryTriggerDistance = 0.25;
    private double legMovingTriggerDistance = 0.9;
    private double legDiscomfortDistance = 1.2;
    private double gravityAcceleration = 0.08;
    private double airDragCoefficient = 0.02;
    private double bounceFactor = 0.5;
    private double bodyHeight = 1.1;
    private double bodyHeightCorrectionAcceleration = gravityAcceleration * 4;
    private double bodyHeightCorrectionFactor = 0.25;
    private double legStraightenHeight = 1.25;
    private boolean legNoStraighten = false;
    private double legSegmentLength = 1.0;
    private int legSegmentCount = 3;
    private boolean legScanAlternativeGround = true;
    private double legScanHeightBias = 0.5;

    public double getWalkSpeed() {
        return walkSpeed;
    }

    public void setWalkSpeed(double walkSpeed) {
        this.walkSpeed = walkSpeed;
    }

    public double getLegSpeed() {
        return legSpeed;
    }

    public void setLegSpeed(double legSpeed) {
        this.legSpeed = legSpeed;
    }

    public double getWalkAcceleration() {
        return walkAcceleration;
    }

    public void setWalkAcceleration(double walkAcceleration) {
        this.walkAcceleration = walkAcceleration;
    }

    public double getRotateSpeed() {
        return rotateSpeed;
    }

    public void setRotateSpeed(double rotateSpeed) {
        this.rotateSpeed = rotateSpeed;
    }

    public double getLegLiftHeight() {
        return legLiftHeight;
    }

    public void setLegLiftHeight(double legLiftHeight) {
        this.legLiftHeight = legLiftHeight;
    }

    public double getLegDropDistance() {
        return legDropDistance;
    }

    public void setLegDropDistance(double legDropDistance) {
        this.legDropDistance = legDropDistance;
    }

    public double getLegStationaryTriggerDistance() {
        return legStationaryTriggerDistance;
    }

    public void setLegStationaryTriggerDistance(double legStationaryTriggerDistance) {
        this.legStationaryTriggerDistance = legStationaryTriggerDistance;
    }

    public double getLegMovingTriggerDistance() {
        return legMovingTriggerDistance;
    }

    public void setLegMovingTriggerDistance(double legMovingTriggerDistance) {
        this.legMovingTriggerDistance = legMovingTriggerDistance;
    }

    public double getLegDiscomfortDistance() {
        return legDiscomfortDistance;
    }

    public void setLegDiscomfortDistance(double legDiscomfortDistance) {
        this.legDiscomfortDistance = legDiscomfortDistance;
    }

    public double getGravityAcceleration() {
        return gravityAcceleration;
    }

    public void setGravityAcceleration(double gravityAcceleration) {
        this.gravityAcceleration = gravityAcceleration;
    }

    public double getAirDragCoefficient() {
        return airDragCoefficient;
    }

    public void setAirDragCoefficient(double airDragCoefficient) {
        this.airDragCoefficient = airDragCoefficient;
    }

    public double getBounceFactor() {
        return bounceFactor;
    }

    public void setBounceFactor(double bounceFactor) {
        this.bounceFactor = bounceFactor;
    }

    public double getBodyHeight() {
        return bodyHeight;
    }

    public void setBodyHeight(double bodyHeight) {
        this.bodyHeight = bodyHeight;
    }

    public double getBodyHeightCorrectionAcceleration() {
        return bodyHeightCorrectionAcceleration;
    }

    public void setBodyHeightCorrectionAcceleration(double bodyHeightCorrectionAcceleration) {
        this.bodyHeightCorrectionAcceleration = bodyHeightCorrectionAcceleration;
    }

    public double getBodyHeightCorrectionFactor() {
        return bodyHeightCorrectionFactor;
    }

    public void setBodyHeightCorrectionFactor(double bodyHeightCorrectionFactor) {
        this.bodyHeightCorrectionFactor = bodyHeightCorrectionFactor;
    }

    public double getLegStraightenHeight() {
        return legStraightenHeight;
    }

    public void setLegStraightenHeight(double legStraightenHeight) {
        this.legStraightenHeight = legStraightenHeight;
    }

    public boolean isLegNoStraighten() {
        return legNoStraighten;
    }

    public void setLegNoStraighten(boolean legNoStraighten) {
        this.legNoStraighten = legNoStraighten;
    }

    public double getLegSegmentLength() {
        return legSegmentLength;
    }

    public void setLegSegmentLength(double legSegmentLength) {
        this.legSegmentLength = legSegmentLength;
    }

    public int getLegSegmentCount() {
        return legSegmentCount;
    }

    public void setLegSegmentCount(int legSegmentCount) {
        this.legSegmentCount = legSegmentCount;
    }

    public boolean isLegScanAlternativeGround() {
        return legScanAlternativeGround;
    }

    public void setLegScanAlternativeGround(boolean legScanAlternativeGround) {
        this.legScanAlternativeGround = legScanAlternativeGround;
    }

    public double getLegScanHeightBias() {
        return legScanHeightBias;
    }

    public void setLegScanHeightBias(double legScanHeightBias) {
        this.legScanHeightBias = legScanHeightBias;
    }
}
