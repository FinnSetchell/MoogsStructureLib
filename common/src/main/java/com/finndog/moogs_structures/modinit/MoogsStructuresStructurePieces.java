package com.finndog.moogs_structures.modinit;

import com.finndog.moogs_structures.MoogsStructuresCommon;
import com.finndog.moogs_structures.modinit.registry.RegistryEntry;
import com.finndog.moogs_structures.modinit.registry.ResourcefulRegistries;
import com.finndog.moogs_structures.modinit.registry.ResourcefulRegistry;
import com.finndog.moogs_structures.world.structures.pieces.LegacyOceanBottomSinglePoolElement;
import com.finndog.moogs_structures.world.structures.pieces.MirroringSingleJigsawPiece;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;


public final class MoogsStructuresStructurePieces {
    public static final ResourcefulRegistry<StructurePoolElementType<?>> STRUCTURE_POOL_ELEMENT = ResourcefulRegistries.create(BuiltInRegistries.STRUCTURE_POOL_ELEMENT, MoogsStructuresCommon.MODID);
    public static final ResourcefulRegistry<StructurePieceType> STRUCTURE_PIECE = ResourcefulRegistries.create(BuiltInRegistries.STRUCTURE_PIECE, MoogsStructuresCommon.MODID);

    public static final RegistryEntry<StructurePoolElementType<MirroringSingleJigsawPiece>> MIRROR_SINGLE = STRUCTURE_POOL_ELEMENT.register("mirroring_single_pool_element", () -> () -> MirroringSingleJigsawPiece.CODEC);
    public static final RegistryEntry<StructurePoolElementType<LegacyOceanBottomSinglePoolElement>> LEGACY_OCEAN_BOTTOM = STRUCTURE_POOL_ELEMENT.register("legacy_ocean_bottom_single_pool_element", () -> () -> LegacyOceanBottomSinglePoolElement.CODEC);
}
