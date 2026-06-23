package com.jtprince.coordinateoffset.paper.lib.org.geysermc.hurricane;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.PointedDripstone;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BoundingBox;

import java.lang.reflect.*;

public final class CollisionFix implements Listener {
    private final boolean bambooEnabled;
    private final BoundingBox originalBambooBoundingBox = box(6.5D, 0.0D, 6.5D, 9.5D, 16D, 9.5D);

    private final boolean pointedDripstoneEnabled;
    private final BoundingBox tipMergeDripstoneBox = box(5D, 0D, 5D, 11D, 16D, 11D);
    private final BoundingBox tipUpDripstoneBox = box(5D, 0D, 5D, 11D, 11D, 11D);
    private final BoundingBox tipDownDripstoneBox = box(5D, 5D, 5D, 11D, 16D, 11D);
    private final BoundingBox frustumDripstoneBox = box(4D, 0D, 4D, 12D, 16D, 12D);
    private final BoundingBox middleDripstoneBox = box(3D, 0D, 3D, 13D, 16D, 13D);
    private final BoundingBox baseDripstoneBox = box(2D, 0D, 2D, 14D, 16D, 14D);

    public CollisionFix(Plugin plugin, boolean bambooEnabled, boolean pointedDripstoneEnabled) {
        // Make any given block have zero collision. Lagback solved...!
        this.bambooEnabled = bambooEnabled;
        this.pointedDripstoneEnabled = pointedDripstoneEnabled;

        if (bambooEnabled) {
            try {
                final Class<?> bambooBlockClass = NMSReflection.getNMSClass("world.level.block", "BambooStalkBlock", "BlockBamboo");
                // CoordinateOffset start - Improve bamboo collision box field lookup to try Mojang-mapped names first
                Field bambooBoundingBox;
                bambooBoundingBox = ReflectionAPI.getFieldAccessible(bambooBlockClass, "SHAPE_COLLISION"); // 1.21.5+ Mojang
                if (bambooBoundingBox != null) {
                    plugin.getLogger().fine("Found bamboo collision hack reflection mapping: SHAPE_COLLISION");
                }
                if (bambooBoundingBox == null) {
                    bambooBoundingBox = ReflectionAPI.getFieldAccessible(bambooBlockClass, "COLLISION_SHAPE"); // 1.17-1.21.4 Mojang
                    if (bambooBoundingBox != null) {
                        plugin.getLogger().fine("Found bamboo collision hack reflection mapping: COLLISION_SHAPE");
                    }
                }
                if (bambooBoundingBox == null) {
                    // If Mojang-mapped names failed, only obfuscated names are left to try.
                    // See https://mappings.dev/history/d57353968b.html
                    // Use https://minecraft.wiki/w/Data_version to map to a version string.
                    int dataVersion = Bukkit.getUnsafe().getDataVersion();
                    plugin.getLogger().fine("Bamboo collision hack reflection mappings are obfuscated; using data version " + dataVersion);
                    if (dataVersion >= 4325) { // 1.21.5+
                        bambooBoundingBox = ReflectionAPI.getFieldAccessible(bambooBlockClass, "S");
                    } else if (dataVersion >= 3700) { // 1.20.4 - 1.21.4
                        bambooBoundingBox = ReflectionAPI.getFieldAccessible(bambooBlockClass, "g");
                    } else if (dataVersion >= 2724) { // 1.17 - 1.20.3
                        bambooBoundingBox = ReflectionAPI.getFieldAccessible(bambooBlockClass, "f");
                    } else { // < 1.17 (Not supported by CoordinateOffset)
                        bambooBoundingBox = ReflectionAPI.getFieldAccessible(bambooBlockClass, "c");
                    }
                }
                // CoordinateOffset end
                applyNoBoundingBox(bambooBoundingBox);
                plugin.getLogger().info("Bamboo collision hack enabled.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (pointedDripstoneEnabled) {
            // We need to disable all dripstone collision, and there's six...
            try {
                // CoordinateOffset start - 26.2 generified "Dripstone" to "Speleothem" to support sulfur spikes.
                Class<?> speleothemBlockClass = NMSReflection.getMojmapNMSClass("world.level.block.SpeleothemBlock");
                if (speleothemBlockClass == null) {
                    // <26.2: Speleothem block classes don't exist yet, bounding box fields are on PointedDripstoneBlock
                    speleothemBlockClass = NMSReflection.getMojmapNMSClass("world.level.block.PointedDripstoneBlock");
                }
                // CoordinateOffset end
                // The method names change between versions, but there's always six next to each other.
                // There is one we do not need to touch (1.18+) because it doesn't deal with collision.
                boolean foundBoundingBoxes = false;
                int boundingBoxCount = 0;
                for (Field field : speleothemBlockClass.getDeclaredFields()) {  // CoordinateOffset
                    if (boundingBoxCount >= 6) {
                        // Don't apply more than necessary
                        break;
                    }
                    if (Modifier.isStatic(field.getModifiers()) && field.getType().getSimpleName().equals("VoxelShape")) {
                        foundBoundingBoxes = true;
                        boundingBoxCount++;
                        applyNoBoundingBox(field);
                    } else if (foundBoundingBoxes) {
                        break;
                    }
                }
                // CoordinateOffset start - log error if no fields found
                if (foundBoundingBoxes) {
                    plugin.getLogger().info("Dripstone collision hack enabled.");
                } else {
                    plugin.getLogger().severe("Failed to enable dripstone collision hack.");
                }
                // CoordinateOffset end
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Because the "fixed" blocks have an empty bounding box, they can be placed inside players... prevent that to the best of
     * our ability.
     */
    @EventHandler
    public void onBlockPlace(final BlockPlaceEvent event) {
        final Block placed = event.getBlockPlaced();
        final Material material = placed.getType();
        if (this.bambooEnabled && material.equals(Material.BAMBOO)) {
            testIfCanBuild(event, this.originalBambooBoundingBox);
        } else if (this.pointedDripstoneEnabled && material.equals(Material.POINTED_DRIPSTONE)) {
            final PointedDripstone data = (PointedDripstone) placed.getBlockData();
            final BoundingBox boundingBox;
            switch (data.getThickness()) {
                case TIP:
                    boundingBox = data.getVerticalDirection() == BlockFace.DOWN ? tipDownDripstoneBox : tipUpDripstoneBox;
                    break;
                case TIP_MERGE:
                    boundingBox = tipMergeDripstoneBox;
                    break;
                case FRUSTUM:
                    boundingBox = frustumDripstoneBox;
                    break;
                case MIDDLE:
                    boundingBox = middleDripstoneBox;
                    break;
                case BASE:
                default:
                    boundingBox = baseDripstoneBox;
                    break;
            }
            testIfCanBuild(event, boundingBox);
        }
    }

    private void testIfCanBuild(final BlockPlaceEvent event, final BoundingBox box) {
        final BoundingBox currentBoundingBox = box.clone().shift(event.getBlockPlaced().getLocation());
        if (event.getPlayer().getBoundingBox().overlaps(currentBoundingBox)) {
            // Don't place this block as it intersects
            event.setBuild(false);
        }
    }

    /**
     * Emulates NMS Block#box
     */
    private BoundingBox box(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return new BoundingBox(minX / 16D, minY / 16D, minZ / 16D, maxX / 16D, maxY / 16D, maxZ / 16D);
    }

    private static void applyNoBoundingBox(Field field) throws NoSuchMethodException, IllegalAccessException,
            InvocationTargetException, InstantiationException {
        final double x1 = 0, y1 = 0, z1 = 0, x2 = 0, y2 = 0, z2 = 0;
        if (field.getType().getSimpleName().equals("AxisAlignedBB")) {
            Class<?> boundingBoxClass = field.getType();
            Constructor<?> boundingBoxConstructor = boundingBoxClass.getConstructor(double.class, double.class, double.class,
                    double.class, double.class, double.class);
            Object boundingBox = boundingBoxConstructor.newInstance(x1, y1, z1, x2, y2, z2);
            ReflectionAPI.setFinalValue(field, boundingBox);
        } else if (field.getType().getSimpleName().equals("VoxelShape")) {
            Method createVoxelShape;
            try {
                // 1.18+ - obfuscated methods
                createVoxelShape = ReflectionAPI.getMethod(NMSReflection.getNMSClass("world.phys.shapes", "Shapes", "VoxelShapes"), "b",
                        double.class, double.class, double.class, double.class, double.class, double.class);
            } catch (NoSuchMethodException e) {
                createVoxelShape = ReflectionAPI.getMethod(NMSReflection.getNMSClass("world.phys.shapes", "Shapes", "VoxelShapes"), "create",
                        double.class, double.class, double.class, double.class, double.class, double.class);
            }
            Object boundingBox = ReflectionAPI.invokeMethod(createVoxelShape, x1, y1, z1, x2, y2, z2);
            ReflectionAPI.setFinalValue(field, boundingBox);
        } else {
            throw new IllegalStateException();
        }
    }
}
