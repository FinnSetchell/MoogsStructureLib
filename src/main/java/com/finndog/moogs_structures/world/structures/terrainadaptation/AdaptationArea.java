package com.finndog.moogs_structures.world.structures.terrainadaptation;

import com.finndog.moogs_structures.MoogsStructuresCommon;
import com.finndog.moogs_structures.mixins.structures.PoolElementStructurePieceAccessor;
import com.finndog.moogs_structures.mixins.structures.SinglePoolElementAccessor;
import com.finndog.moogs_structures.mixins.structures.TemplateAccessor;
import com.finndog.moogs_structures.utils.StrictOptionalField;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The columns of a rigid piece that MSL's enhanced terrain adaptation works around, set on the pool
 * element as {@code adaptation_area}. Without one, the adaptation shapes the ground around the piece's
 * whole bounding box; with one, only around these columns, so ground decoration outside them (a
 * flower ring, a tree's canopy) is left alone. The height still comes from the piece and its
 * adaptation settings (box, padding, band).
 *
 * <p>The columns are either listed ({@code columns}, in template coordinates, as seen from the
 * structure block that saved the piece) or found from the template's own blocks ({@code footprint}:
 * the columns holding a matching block in the lowest {@code layers} layers, as one rectangle). With
 * {@code outside_follows_ground}, every column of the piece outside the area follows the ground the
 * way {@code moogs_structures:follow_floor_processor} does, so a rigid core can sit in a layer that
 * follows the slope.
 */
public final class AdaptationArea {
    public static final Codec<AdaptationArea> CODEC = RecordCodecBuilder.<AdaptationArea>create(instance -> instance.group(
            StrictOptionalField.of(ColumnRect.CODEC.listOf(), "columns").forGetter(a -> a.columns),
            StrictOptionalField.of(Footprint.CODEC, "footprint").forGetter(a -> a.footprint),
            StrictOptionalField.of(Codec.BOOL, "outside_follows_ground", false).forGetter(a -> a.outsideFollowsGround)
    ).apply(instance, AdaptationArea::new)).flatXmap(AdaptationArea::check, AdaptationArea::check);

    private static DataResult<AdaptationArea> check(AdaptationArea area) {
        if (area.columns.isPresent() == area.footprint.isPresent()) {
            return DataResult.error(() -> "adaptation_area needs exactly one of \"columns\" or \"footprint\"");
        }
        if (area.columns.isPresent() && area.columns.get().isEmpty()) {
            return DataResult.error(() -> "adaptation_area \"columns\" needs at least one rectangle");
        }
        return DataResult.success(area);
    }

    /**
     * Set when a server starts, for reading footprints where no template manager is passed in. Held weakly:
     * the server keeps it while it runs, and a closed world's templates and registries aren't kept after.
     */
    private static volatile WeakReference<StructureTemplateManager> serverTemplates = new WeakReference<>(null);

    private final Optional<List<ColumnRect>> columns;
    private final Optional<Footprint> footprint;
    private final boolean outsideFollowsGround;
    @Nullable
    private volatile List<ColumnRect> resolved;

    private AdaptationArea(Optional<List<ColumnRect>> columns, Optional<Footprint> footprint, boolean outsideFollowsGround) {
        this.columns = columns.map(List::copyOf);
        this.footprint = footprint;
        this.outsideFollowsGround = outsideFollowsGround;
        this.resolved = this.columns.orElse(null);
    }

    public static void setServerTemplates(@Nullable StructureTemplateManager manager) {
        serverTemplates = new WeakReference<>(manager);
    }

    public boolean outsideFollowsGround() {
        return this.outsideFollowsGround;
    }

    /**
     * The area's columns in template coordinates, or null while they can't be read yet. A footprint is
     * read from the template the first time and kept, as the template behind an element doesn't change
     * while a server runs.
     */
    @Nullable
    public List<ColumnRect> localColumns(SinglePoolElement element, @Nullable StructureTemplateManager manager) {
        List<ColumnRect> cached = this.resolved;
        if (cached != null) {
            return cached;
        }
        StructureTemplateManager templates = manager != null ? manager : serverTemplates.get();
        if (templates == null) {
            return null;
        }
        StructureTemplate template = ((SinglePoolElementAccessor) element).callGetTemplate(templates);
        List<ColumnRect> found = this.footprint.orElseThrow().find(template).map(List::of).orElse(List.of());
        if (found.isEmpty()) {
            MoogsStructuresCommon.LOGGER.warn("Moog's Structure Lib: the adaptation_area footprint of {} matches no blocks, so it shapes no ground", element);
        }
        this.resolved = found;
        return found;
    }

    /**
     * The area's columns where this piece sits in the world, each with the piece's own y extent and
     * cut to the piece's box. Null when the columns can't be read, so the caller falls back to the box.
     */
    @Nullable
    public List<BoundingBox> worldColumns(PoolElementStructurePiece piece) {
        if (!(piece.getElement() instanceof SinglePoolElement element)) {
            return null;
        }
        List<ColumnRect> local = localColumns(element, ((PoolElementStructurePieceAccessor) piece).getStructureManager());
        if (local == null) {
            return null;
        }
        BoundingBox box = piece.getBoundingBox();
        List<BoundingBox> world = new ArrayList<>(local.size());
        for (ColumnRect rect : local) {
            BlockPos a = StructureTemplate.transform(new BlockPos(rect.minX(), 0, rect.minZ()), Mirror.NONE, piece.getRotation(), BlockPos.ZERO).offset(piece.getPosition());
            BlockPos b = StructureTemplate.transform(new BlockPos(rect.maxX(), 0, rect.maxZ()), Mirror.NONE, piece.getRotation(), BlockPos.ZERO).offset(piece.getPosition());
            int minX = Math.max(box.minX(), Math.min(a.getX(), b.getX()));
            int maxX = Math.min(box.maxX(), Math.max(a.getX(), b.getX()));
            int minZ = Math.max(box.minZ(), Math.min(a.getZ(), b.getZ()));
            int maxZ = Math.min(box.maxZ(), Math.max(a.getZ(), b.getZ()));
            if (minX <= maxX && minZ <= maxZ) {
                world.add(new BoundingBox(minX, box.minY(), minZ, maxX, box.maxY(), maxZ));
            }
        }
        return world;
    }

    /** A rectangle of template columns, both ends included. JSON: {"x": [min, max], "z": [min, max]}. */
    public record ColumnRect(int minX, int maxX, int minZ, int maxZ) {
        private static final Codec<int[]> RANGE = Codec.INT.listOf().comapFlatMap(
                list -> list.size() == 2 && list.get(0) <= list.get(1)
                        ? DataResult.success(new int[]{list.get(0), list.get(1)})
                        : DataResult.error(() -> "a column range is [min, max] with min <= max, got " + list),
                range -> List.of(range[0], range[1]));

        public static final Codec<ColumnRect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                RANGE.fieldOf("x").forGetter(r -> new int[]{r.minX, r.maxX}),
                RANGE.fieldOf("z").forGetter(r -> new int[]{r.minZ, r.maxZ})
        ).apply(instance, (x, z) -> new ColumnRect(x[0], x[1], z[0], z[1])));

        public boolean contains(int x, int z) {
            return x >= this.minX && x <= this.maxX && z >= this.minZ && z <= this.maxZ;
        }

        public static boolean anyContains(List<ColumnRect> rects, int x, int z) {
            for (ColumnRect rect : rects) {
                if (rect.contains(x, z)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * The columns whose lowest {@code layers} layers (counted from the template's lowest block) hold a
     * block in {@code blocks}, or any block when no tag is given, as one rectangle grown by
     * {@code margin} on every side. Air, structure voids, jigsaws and structure blocks never count.
     */
    public record Footprint(int layers, Optional<TagKey<Block>> blocks, int margin) {
        public static final Codec<Footprint> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                StrictOptionalField.of(Codec.intRange(1, 64), "layers", 1).forGetter(Footprint::layers),
                StrictOptionalField.of(TagKey.hashedCodec(Registries.BLOCK), "blocks").forGetter(Footprint::blocks),
                StrictOptionalField.of(Codec.intRange(0, 16), "margin", 0).forGetter(Footprint::margin)
        ).apply(instance, Footprint::new));

        Optional<ColumnRect> find(StructureTemplate template) {
            List<StructureTemplate.Palette> palettes = ((TemplateAccessor) template).moogs_structures_getPalettes();
            if (palettes.isEmpty()) {
                return Optional.empty();
            }
            List<StructureTemplate.StructureBlockInfo> blocks = palettes.get(0).blocks();
            int lowest = Integer.MAX_VALUE;
            for (StructureTemplate.StructureBlockInfo info : blocks) {
                if (counts(info.state())) {
                    lowest = Math.min(lowest, info.pos().getY());
                }
            }
            int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
            for (StructureTemplate.StructureBlockInfo info : blocks) {
                BlockPos pos = info.pos();
                if (pos.getY() - lowest < this.layers && counts(info.state())
                        && (this.blocks.isEmpty() || info.state().is(this.blocks.get()))) {
                    minX = Math.min(minX, pos.getX());
                    maxX = Math.max(maxX, pos.getX());
                    minZ = Math.min(minZ, pos.getZ());
                    maxZ = Math.max(maxZ, pos.getZ());
                }
            }
            if (minX == Integer.MAX_VALUE) {
                return Optional.empty();
            }
            return Optional.of(new ColumnRect(minX - this.margin, maxX + this.margin, minZ - this.margin, maxZ + this.margin));
        }

        private static boolean counts(BlockState state) {
            return !state.isAir() && !state.is(Blocks.STRUCTURE_VOID) && !state.is(Blocks.JIGSAW) && !state.is(Blocks.STRUCTURE_BLOCK);
        }
    }
}
