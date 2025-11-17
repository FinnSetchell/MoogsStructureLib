package com.finndog.moogs_structures.commands;

import com.finndog.moogs_structures.MoogsStructuresCommon;
import com.finndog.moogs_structures.utils.DebugFlags;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class DebugCommand {

    private DebugCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal(MoogsStructuresCommon.MODID)
                        .requires(source -> source.hasPermission(2))
                        .then(
                                Commands.literal("debug")
                                        .executes(context -> toggle(context.getSource()))
                                        .then(Commands.literal("on")
                                                .executes(context -> set(context.getSource(), true)))
                                        .then(Commands.literal("off")
                                                .executes(context -> set(context.getSource(), false)))
                                        .then(Commands.literal("status")
                                                .executes(context -> report(context.getSource())))
                        )
        );
    }

    private static int toggle(CommandSourceStack source) {
        boolean enabled = DebugFlags.toggle();
        return notify(source, "toggled", enabled);
    }

    private static int set(CommandSourceStack source, boolean value) {
        boolean enabled = DebugFlags.setEnabled(value);
        return notify(source, value ? "enabled" : "disabled", enabled);
    }

    private static int report(CommandSourceStack source) {
        boolean enabled = DebugFlags.isEnabled();
        source.sendSuccess(() ->
                Component.literal("Moog's Structure debug mode is " + (enabled ? "enabled" : "disabled")), false);
        return enabled ? 1 : 0;
    }

    private static int notify(CommandSourceStack source, String action, boolean enabled) {
        String message = "Moog's Structure debug mode " + action + " (" + (enabled ? "enabled" : "disabled") + ")";
        MoogsStructuresCommon.LOGGER.info(message);
        source.sendSuccess(() -> Component.literal(message), true);
        return enabled ? 1 : 0;
    }
}
