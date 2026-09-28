package com.finndog.moogs_structures.mixins.structures;

//? if <26.2 {
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(StructureProcessor.class)
//?}
// In MC 26.2, StructureProcessor became a public interface and getType() was replaced
// by the public codec() method — no accessor mixin is needed. This file is intentionally empty.
// The mixin config entry was removed from moogs_structures-common.mixins.json.
public interface StructureProcessorAccessor {
    //? if <26.2 {
    @Invoker("getType")
    StructureProcessorType<?> callGetType();
    //?}
}
