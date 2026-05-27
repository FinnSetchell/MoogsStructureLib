package com.finndog.moogs_structures.utils;

public final class DebugFlags {

    private static boolean enabled;

    /**
     * When enabled, jigsaw blocks are left intact in placed structures instead of being
     * converted to their final_state. Useful for inspecting piece connections in-world.
     */
    private static volatile boolean keepJigsawBlocks;

    private DebugFlags() {
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static boolean setEnabled(boolean value) {
        enabled = value;
        return enabled;
    }

    public static boolean toggle() {
        enabled = !enabled;
        return enabled;
    }

    public static boolean isKeepJigsawBlocks() {
        return keepJigsawBlocks;
    }

    public static boolean setKeepJigsawBlocks(boolean value) {
        keepJigsawBlocks = value;
        return keepJigsawBlocks;
    }

    public static boolean toggleKeepJigsawBlocks() {
        keepJigsawBlocks = !keepJigsawBlocks;
        return keepJigsawBlocks;
    }
}
