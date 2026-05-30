package com.finndog.moogs_structures.mixins.terrainadaptation;

import net.minecraft.world.level.levelgen.Beardifier;
import org.spongepowered.asm.mixin.Mixin;

// Accessor methods removed: the Beardifier field shape changed from iterator-based
// (pieceIterator/junctionIterator, MC <= 1.21.8) to list-based (pieces/junctions/affectedBox,
// MC >= 1.21.9). Mixin 0.8.5 @Accessor has no require=0 equivalent, so accessing either set
// of fields via @Accessor would crash on whichever half of the version range lacks those fields.
// EnhancedBeardifierHelper now probes and accesses Beardifier fields via reflection instead.
@Mixin(Beardifier.class)
public interface BeardifierAccessor {
}
