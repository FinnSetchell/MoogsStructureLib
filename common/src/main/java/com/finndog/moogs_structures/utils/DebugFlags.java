package com.finndog.moogs_structures.utils;

/**
 * Central toggle for runtime debug behaviour.
 */
public final class DebugFlags {

    private static boolean enabled;

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
}

