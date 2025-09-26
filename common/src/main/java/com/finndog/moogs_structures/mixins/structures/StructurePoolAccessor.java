package com.finndog.moogs_structures.mixins.structures;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(StructureTemplatePool.class)
public interface StructurePoolAccessor {
    @Accessor("CODEC_REFERENCE")
    static MutableObject<Codec<Holder<StructureTemplatePool>>> getCODEC_REFERENCE() {
        throw new UnsupportedOperationException();
    }

    @Accessor("rawTemplates")
    List<Pair<StructurePoolElement, Integer>> moogs_structures_getRawTemplates();

    @Mutable
    @Accessor("rawTemplates")
    void moogs_structures_setRawTemplates(List<Pair<StructurePoolElement, Integer>> elementCounts);

    @Accessor("templates")
    ObjectArrayList<StructurePoolElement> moogs_structures_getTemplates();

    @Mutable
    @Accessor("templates")
    void moogs_structures_setTemplates(ObjectArrayList<StructurePoolElement> elements);
}
