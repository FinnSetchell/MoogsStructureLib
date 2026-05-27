package com.finndog.moogs_structures.modinit;

import com.finndog.moogs_structures.MoogsStructuresCommon;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;

public final class MoogsStructuresTags {
    public static void initTags() {}

    public static TagKey<Structure> LARGER_LOCATE_SEARCH = TagKey.create(Registries.STRUCTURE,
            ResourceLocation.fromNamespaceAndPath(MoogsStructuresCommon.MODID, "larger_locate_search"));

    // Structures in this tag will not have nether basalt columns generate inside them.
    public static TagKey<Structure> NO_BASALT = TagKey.create(Registries.STRUCTURE,
            ResourceLocation.fromNamespaceAndPath(MoogsStructuresCommon.MODID, "no_basalt"));

    // Structures in this tag will not have nether basalt-delta lava blobs generate inside them.
    public static TagKey<Structure> NO_DELTA = TagKey.create(Registries.STRUCTURE,
            ResourceLocation.fromNamespaceAndPath(MoogsStructuresCommon.MODID, "no_delta"));

}
