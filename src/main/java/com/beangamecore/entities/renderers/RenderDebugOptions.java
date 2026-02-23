package com.beangamecore.entities.renderers;

public class RenderDebugOptions {
    public boolean legTarget;
    public boolean legTriggerZone;
    public boolean legRestPosition;
    public boolean legEndEffector;
    public boolean spiderDirection;

    public RenderDebugOptions(boolean legTarget, boolean legTriggerZone, boolean legRestPosition, 
                            boolean legEndEffector, boolean spiderDirection) {
        this.legTarget = legTarget;
        this.legTriggerZone = legTriggerZone;
        this.legRestPosition = legRestPosition;
        this.legEndEffector = legEndEffector;
        this.spiderDirection = spiderDirection;
    }

    public static RenderDebugOptions all() {
        return new RenderDebugOptions(true, true, true, true, true);
    }

    public static RenderDebugOptions none() {
        return new RenderDebugOptions(false, false, false, false, false);
    }
}
