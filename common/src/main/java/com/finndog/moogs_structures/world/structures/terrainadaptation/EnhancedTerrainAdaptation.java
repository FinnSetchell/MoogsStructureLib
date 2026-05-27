package com.finndog.moogs_structures.world.structures.terrainadaptation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * A reduced port of YUNG's API "enhanced terrain adaptation" (LGPL, attribution to YungNickYoung /
 * TelepathicGrunt). Uses a 3-D Gaussian kernel to smoothly modify terrain noise density around a
 * structure's pieces, allowing the terrain above/below the structure to be carved or buried with a
 * natural falloff. The aquifer-override and per-pool-element-override features of the original are
 * intentionally omitted from this port.
 */
public class EnhancedTerrainAdaptation {
    /** Sentinel meaning "no adaptation". Kernel radius 0, so it is skipped everywhere. */
    public static final EnhancedTerrainAdaptation NONE =
            new EnhancedTerrainAdaptation(0, 0, TerrainAction.NONE, TerrainAction.NONE, 0.0, Padding.ZERO, Optional.empty());

    public static final Codec<EnhancedTerrainAdaptation> CODEC = RecordCodecBuilder.create(builder -> builder
            .group(
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("kernel_size").forGetter(EnhancedTerrainAdaptation::getKernelSize),
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("kernel_distance").forGetter(EnhancedTerrainAdaptation::getKernelDistance),
                    TerrainAction.CODEC.fieldOf("top").forGetter(EnhancedTerrainAdaptation::topAction),
                    TerrainAction.CODEC.fieldOf("bottom").forGetter(EnhancedTerrainAdaptation::bottomAction),
                    Codec.DOUBLE.optionalFieldOf("bottom_offset", 0.0).forGetter(EnhancedTerrainAdaptation::getBottomOffset),
                    Padding.CODEC.optionalFieldOf("padding", Padding.ZERO).forGetter(EnhancedTerrainAdaptation::getPadding),
                    Band.CODEC.optionalFieldOf("band").forGetter(EnhancedTerrainAdaptation::getBand))
            .apply(builder, EnhancedTerrainAdaptation::new));

    private final TerrainAction topAction;
    private final TerrainAction bottomAction;
    private final int kernelSize;
    private final int kernelDistance;
    private final double bottomOffset;
    private final Padding padding;
    private final Optional<Band> band;
    private final float[] kernel;

    public EnhancedTerrainAdaptation(int kernelSize, int kernelDistance, TerrainAction topAction,
                                     TerrainAction bottomAction, double bottomOffset, Padding padding,
                                     Optional<Band> band) {
        this.kernelSize = kernelSize;
        this.kernelDistance = kernelDistance;
        this.topAction = topAction;
        this.bottomAction = bottomAction;
        this.bottomOffset = bottomOffset;
        this.padding = padding;
        this.band = band;
        int kernelRadius = this.getKernelRadius();
        this.kernel = Util.make(new float[kernelSize * kernelSize * kernelSize], (k) -> {
            for (int x = 0; x < kernelSize; ++x) {
                for (int y = 0; y < kernelSize; ++y) {
                    for (int z = 0; z < kernelSize; ++z) {
                        int i = index(x, y, z);
                        double kernelX = x - kernelRadius;
                        double kernelY = y - kernelRadius + 0.5;
                        double kernelZ = z - kernelRadius;
                        k[i] = computeKernelValue(kernelX, kernelY, kernelZ);
                    }
                }
            }
        });
    }

    private float computeKernelValue(double xDistance, double yDistance, double zDistance) {
        double squaredDistance = Mth.lengthSquared(xDistance, yDistance, zDistance);
        return (float) Math.pow(Math.E, -squaredDistance / this.kernelDistance);
    }

    public TerrainAction topAction() { return this.topAction; }
    public TerrainAction bottomAction() { return this.bottomAction; }
    public double getBottomOffset() { return this.bottomOffset; }
    public Padding getPadding() { return this.padding; }
    public int getKernelSize() { return this.kernelSize; }
    public int getKernelRadius() { return this.kernelSize / 2; }
    public int getKernelDistance() { return this.kernelDistance; }
    public float[] getKernel() { return this.kernel; }
    public Optional<Band> getBand() { return this.band; }

    /**
     * Computes the noise density factor at a single location. Positive buries, negative carves.
     */
    public double computeDensityFactor(int xDistance, int yDistance, int zDistance, int yDistanceToPieceBottom) {
        int kernelRadius = this.getKernelRadius();
        int kernelX = xDistance + kernelRadius;
        int kernelY = yDistance + kernelRadius;
        int kernelZ = zDistance + kernelRadius;
        if (isInKernelRange(kernelX) && isInKernelRange(kernelY) && isInKernelRange(kernelZ)) {
            int i = index(kernelX, kernelY, kernelZ);
            float kernelValue = this.getKernel()[i];

            double actualYDistanceToPieceBottom = (double) yDistanceToPieceBottom + 0.5;
            double squaredDistance = Mth.lengthSquared(xDistance, actualYDistanceToPieceBottom, zDistance);
            double multiplier = Math.abs(actualYDistanceToPieceBottom * Mth.invSqrt(squaredDistance / 2.0) / 2.0);

            boolean isAboveBeardBase = actualYDistanceToPieceBottom > 0;
            int densityModifier = isAboveBeardBase ? this.topAction.getDensityModifier() : this.bottomAction.getDensityModifier();

            return multiplier * kernelValue * densityModifier;
        } else {
            return 0;
        }
    }

    private boolean isInKernelRange(int i) {
        return i >= 0 && i < this.kernelSize;
    }

    private int index(int x, int y, int z) {
        return z * this.kernelSize * this.kernelSize + x * this.kernelSize + y;
    }

    /**
     * Restricts terrain adaptation to a vertical band of each piece, expressed in piece-local
     * rows (0 = piece floor). When present, the adapted region's Y extent is clamped to
     * [floor + bottom, floor + top] and the top/bottom padding is ignored. If {@code pieceHeights}
     * is set, the band only applies to rigid pieces whose Y-span is one of the listed heights (so,
     * e.g., only 24-, 25-, and 27-block-tall corridors are affected, not crossings or end caps).
     */
    public record Band(int bottom, int top, Optional<List<Integer>> pieceHeights) {
        public static final Codec<Band> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                Codec.INT.fieldOf("bottom").forGetter(Band::bottom),
                Codec.INT.fieldOf("top").forGetter(Band::top),
                Codec.INT.listOf().optionalFieldOf("piece_heights").forGetter(Band::pieceHeights)
        ).apply(instance, Band::new));
    }

    /** Padding to expand/shrink the piece bounding box per-axis before adapting terrain. */
    public record Padding(int x, int top, int bottom, int z) {
        public static final Padding ZERO = new Padding(0, 0, 0, 0);
        public static final Codec<Padding> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                Codec.INT.optionalFieldOf("x", 0).forGetter((padding) -> padding.x),
                Codec.INT.optionalFieldOf("top", 0).forGetter((padding) -> padding.top),
                Codec.INT.optionalFieldOf("bottom", 0).forGetter((padding) -> padding.bottom),
                Codec.INT.optionalFieldOf("z", 0).forGetter((padding) -> padding.z)
        ).apply(instance, Padding::new));
    }

    /** carve = remove terrain (-1), bury = add terrain (+1), none = no change (0). */
    public enum TerrainAction implements StringRepresentable {
        CARVE("carve", -1),
        BURY("bury", 1),
        NONE("none", 0);

        public static final Codec<TerrainAction> CODEC = StringRepresentable.fromEnum(TerrainAction::values);
        private final String name;
        private final int densityModifier;

        TerrainAction(String name, int densityModifier) {
            this.name = name;
            this.densityModifier = densityModifier;
        }

        public int getDensityModifier() { return densityModifier; }

        @Override
        public @NotNull String getSerializedName() { return name; }
    }
}
