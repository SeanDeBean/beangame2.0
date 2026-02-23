package com.beangamecore.entities.renderers;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.beangamecore.entities.tntspider.Leg;
import com.beangamecore.entities.tntspider.Spider;
import com.mysql.cj.conf.ConnectionUrlParser.Pair;

public class BlockDisplayRenderer implements Renderer {

    private static final Map<Object, BlockDisplay> displays = new HashMap<>();

    public enum Identifier {
        TARGET,
        LEG_TARGET_POSITION,
        LEG_TRIGGER_ZONE,
        LEG_REST_POSITION,
        LEG_REST_POSITION_CENTER,
        LEG_END_EFFECTOR,
        DIRECTION;

        public static Object chainSegment(ChainSegment segment) {
            return segment;
        }

        public static Object legTargetPosition(Leg leg) {
            return new Pair<>(LEG_TARGET_POSITION, leg);
        }

        public static Object legTriggerZone(Leg leg) {
            return new Pair<>(LEG_TRIGGER_ZONE, leg);
        }

        public static Object legRestPosition(Leg leg) {
            return new Pair<>(LEG_REST_POSITION, leg);
        }

        public static Object legRestPositionCenter(Leg leg) {
            return new Pair<>(LEG_REST_POSITION_CENTER, leg);
        }

        public static Object legEndEffector(Leg leg) {
            return new Pair<>(LEG_END_EFFECTOR, leg);
        }

        public static Object direction(Spider spider) {
            return new Pair<>(DIRECTION, spider);
        }

        public static Object target(Object id) {
            return new Pair<>(TARGET, id);
        }
    }

    public static BlockDisplay render(Object identifier, Location location, Consumer<BlockDisplay> init) {
        // if (displays.size() > 100) {
        //     throw new IllegalStateException("Too many displays. Check for memory leaks.");
        // }

        BlockDisplay entity = displays.computeIfAbsent(identifier, id -> location.getWorld().spawn(location, BlockDisplay.class, init));
        entity.teleport(location);
        return entity;
    }

    public BlockDisplay renderIf(Object identifier, Location location, boolean condition, Consumer<BlockDisplay> init) {
        if (!condition) {
            clear(identifier);
            return null;
        }
        return render(identifier, location, init);
    }

    @Override
    public void clear(Object identifier) {
        BlockDisplay display = displays.remove(identifier);
        if (display != null) {
            display.remove();
        }
    }

    public static void clearAll() {
        for (BlockDisplay entity : displays.values()) {
            entity.remove();
        }
        displays.clear();
    }

    @Override
    public void renderSpider(Spider spider, RenderDebugOptions debug) {
        for (Leg leg : spider.getLegs()) {
            renderLeg(leg, debug);
        }
        renderDirection(spider, debug.spiderDirection);
        if (spider.didHitGround()) {
            spider.getLocation().getWorld().playSound(spider.getLocation(), Sound.BLOCK_NETHERITE_BLOCK_FALL, 1.0f, 0.8f);
        }
    }

    @Override
    public void clearSpider(Spider spider) {
        for (Leg leg : spider.getLegs()) {
            for (ChainSegment segment : leg.getChain().getSegments()) {
                clear(Identifier.chainSegment(segment));
            }
            clear(Identifier.legTargetPosition(leg));
            clear(Identifier.legTriggerZone(leg));
            clear(Identifier.legRestPosition(leg));
            clear(Identifier.legRestPositionCenter(leg));
            clear(Identifier.legEndEffector(leg));
        }
        clear(Identifier.direction(spider));
    }

    public void renderDirection(Spider spider, boolean render) {
        Location location = spider.getLocation().clone();
        location.add(spider.getLocation().getDirection());

        BlockDisplay display = renderIf(Identifier.direction(spider), location, render, d -> {
            d.setTeleportDuration(2);
            Display.Brightness brightness = new Display.Brightness(15, 15);
            d.setBrightness(brightness);
            float size = 0.1f;
            d.setTransformation(centredTransform(size, size, size));
        });

        if (display != null) {
            display.setBlock(Material.EMERALD_BLOCK.createBlockData());
            display.teleport(location);
        }
    }

    @Override
    public void renderTarget(Location location, Object identifier) {
        render(identifier, location, display -> {
            display.setBlock(Material.REDSTONE_BLOCK.createBlockData());
            display.setTeleportDuration(2);
            Display.Brightness brightness = new Display.Brightness(15, 15);
            display.setBrightness(brightness);
            float size = 0.25f;
            display.setTransformation(centredTransform(size, size, size));
        });
    }

    public void renderLeg(Leg leg, RenderDebugOptions debug) {
        Location root = leg.getChain().getRoot().toLocation(leg.getParent().getLocation().getWorld());
        Vector position = leg.getChain().getSegments().get(leg.getChain().getSegments().size() - 1).getPosition().clone();
        Vector dir = position.subtract(root.toVector());
        Vector upVector = Utils.crossProduct(dir, new Vector(0, 1, 0));

        float maxThickness = 1.5f / 16f * 4;
        float minThickness = 1.5f / 16f * 1;

        for (int i = 0; i < leg.getChain().getSegments().size(); i++) {
            ChainSegment segment = leg.getChain().getSegments().get(i);
            float thickness = (leg.getChain().getSegments().size() - i - 1) * (maxThickness - minThickness) / leg.getChain().getSegments().size() + minThickness;
            renderSegment(root, segment, thickness, upVector);
            root = segment.getPosition().toLocation(leg.getParent().getLocation().getWorld());
        }

        renderLegTargetPosition(leg, debug.legTarget);
        renderLegTriggerZone(leg, debug.legTriggerZone);
        renderLegRestPosition(leg, debug.legRestPosition);
        renderLegEndEffector(leg, debug.legEndEffector);

        if (leg.didStep()) {
            Location location = leg.getEndEffector().toLocation(leg.getParent().getLocation().getWorld());
            float volume = 0.3f;
            float pitch = 1.0f + (float) Math.random() * 0.1f;
            location.getWorld().playSound(location, Sound.BLOCK_NETHERITE_BLOCK_STEP, volume, pitch);
        }
    }

    public void renderLegTargetPosition(Leg leg, boolean renderDebug) {
        Location location = leg.getTargetPosition().toLocation(leg.getParent().getLocation().getWorld());
        renderIf(Identifier.legTargetPosition(leg), location, renderDebug, display -> {
            display.setTeleportDuration(2);
            Display.Brightness brightness = new Display.Brightness(15, 15);
            display.setBrightness(brightness);
            display.setBlock(Material.GREEN_STAINED_GLASS.createBlockData());
            float size = 0.2f;
            display.setTransformation(centredTransform(size, size, size));
        });
    }

    public void renderLegRestPosition(Leg leg, boolean renderDebug) {
        Location location = leg.restPosition().toLocation(leg.getParent().getLocation().getWorld());
        float yMax = (float) (location.getY() + leg.getScanGroundAbove());
        float yMin = (float) (location.getY() - leg.getScanGroundBelow());
        float yCenter = (yMax + yMin) / 2;
        float ySize = yMax - yMin;

        location.setY(yCenter);

        BlockDisplay display1 = renderIf(Identifier.legRestPosition(leg), location, renderDebug, d -> {
            d.setTeleportDuration(2);
            d.setBrightness(new Display.Brightness(15, 15));
            float size = 0.05f;
            d.setTransformation(centredTransform(size, ySize, size));
        });

        if (display1 != null) {
            display1.setBlock(leg.isStranded() ? Material.COPPER_BLOCK.createBlockData() : Material.GOLD_BLOCK.createBlockData());
            display1.teleport(location);
        }

        BlockDisplay display2 = renderIf(Identifier.legRestPositionCenter(leg), location, renderDebug, d -> {
            d.setTeleportDuration(2);
            d.setBrightness(new Display.Brightness(15, 15));
            float size = 0.1f;
            float ySizeCenter = 0.05f;
            d.setTransformation(centredTransform(size, ySizeCenter, size));
        });

        if (display2 != null) {
            display2.setBlock(leg.isStranded() ? Material.COPPER_BLOCK.createBlockData() : Material.GOLD_BLOCK.createBlockData());
            display2.teleport(location);
        }
    }

    public void renderLegTriggerZone(Leg leg, boolean renderDebug) {
        Location location = leg.getTargetPosition().toLocation(leg.getParent().getLocation().getWorld());
        BlockDisplay display = renderIf(Identifier.legTriggerZone(leg), location, renderDebug, d -> {
            d.setTeleportDuration(2);
            Display.Brightness brightness = new Display.Brightness(15, 15);
            d.setBrightness(brightness);
            d.setInterpolationDuration(2);
        });
        if (display != null) { // or if you know it never returns null, no need for this check
            float size = 2 * (float) leg.triggerDistance();
            float ySize = 0.02f;
            Transformation transform = centredTransform(size, ySize, size);

            BlockData blockData = leg.isUncomfortable()
                ? Material.RED_STAINED_GLASS.createBlockData()
                : Material.CYAN_STAINED_GLASS.createBlockData();

            display.setBlock(blockData);

            if (!display.getTransformation().equals(transform)) {
                display.setTransformation(transform);
                display.setInterpolationDelay(0);
            }
        }
    }

        public void renderLegEndEffector(Leg leg, boolean renderDebug) {
        Location position = leg.getEndEffector().toLocation(leg.getParent().getLocation().getWorld());
        BlockDisplay display = renderIf(Identifier.legEndEffector(leg), position, renderDebug, d -> {
            d.setTeleportDuration(1);
            d.setBrightness(new Display.Brightness(15, 15));
            float size = 0.15f;
            d.setTransformation(centredTransform(size, size, size));
        });

        if (display != null) {
            display.setBlock(leg.isOnGround() ? Material.DIAMOND_BLOCK.createBlockData() : Material.REDSTONE_BLOCK.createBlockData());
        }
    }

    public static BlockDisplay renderSegment(Location location, ChainSegment segment, float thickness, Vector upVector) {
        return renderSegment(location, segment, thickness, upVector, Material.NETHERITE_BLOCK);
    }

    public static BlockDisplay renderTentacleSegment(Location location, ChainSegment segment, float thickness, Vector upVector) {
        return renderSegment(location, segment, thickness, upVector, Material.DARK_PRISMARINE);
    }

    private static BlockDisplay renderSegment(Location location, ChainSegment segment, float thickness, Vector upVector, Material material) {
        float xSize = thickness;
        float ySize = thickness;
        float zSize = (float) segment.getLength();
        
        BlockDisplay display = render(Identifier.chainSegment(segment), location, d -> {
            d.setBlock(material.createBlockData());
            d.setTeleportDuration(2);
            d.setInterpolationDuration(2);
        });

        Vector vector = segment.getPosition().clone().subtract(location.toVector());
        Matrix4f matrix = new Matrix4f();
        Vector3f vectorJoml = new Vector3f((float) vector.getX(), (float) vector.getY(), (float) vector.getZ());
        Vector3f upVectorJoml = upVector.toVector3f();
        matrix.rotateTowards(vectorJoml, upVectorJoml);
        matrix.translate(-xSize / 2, -ySize / 2, 0f);
        matrix.scale(xSize, ySize, zSize);

        Transformation transform = transformFromMatrix(matrix);
        if (!display.getTransformation().equals(transform)) {
            display.setTransformation(transform);
            display.setInterpolationDelay(0);
        }

        return display;
    }

    public Transformation centredTransform(float xSize, float ySize, float zSize) {
        return new Transformation(
            new Vector3f(-xSize / 2, -ySize / 2, -zSize / 2),
            new AxisAngle4f(0f, 0f, 0f, 1f),
            new Vector3f(xSize, ySize, zSize),
            new AxisAngle4f(0f, 0f, 0f, 1f)
        );
    }

    public static Transformation transformFromMatrix(Matrix4f matrix) {
        Vector3f translation = matrix.getTranslation(new Vector3f());
        Quaternionf rotation = matrix.getUnnormalizedRotation(new Quaternionf());
        Vector3f scale = matrix.getScale(new Vector3f());
        return new Transformation(translation, rotation, scale, new Quaternionf());
    }
}

