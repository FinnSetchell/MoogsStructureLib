package com.finndog.moogs_structures.world.structures;

import net.minecraft.world.level.levelgen.structure.Structure;
import org.jetbrains.annotations.Nullable;

/**
 * The MSL structure placing its pieces in a chunk on this thread, if any. The StructureStart mixin sets it, so
 * placement fixes the game can't make itself on older versions (rail rotation, liquid settings before 1.21)
 * reach only MSL structures and leave vanilla's and other mods' structures alone.
 */
public final class PlacingStructure {
    private static final ThreadLocal<GenericJigsawStructure> CURRENT = new ThreadLocal<>();

    private PlacingStructure() {}

    /** Called as a structure starts placing its pieces in a chunk. */
    public static void start(Structure structure) {
        CURRENT.set(structure instanceof GenericJigsawStructure mslStructure ? mslStructure : null);
    }

    /** Called when that structure has finished placing in the chunk. */
    public static void stop() {
        CURRENT.remove();
    }

    /** The MSL structure placing now, or null when none is (vanilla, another mod's, or no structure). */
    @Nullable
    public static GenericJigsawStructure get() {
        return CURRENT.get();
    }
}
