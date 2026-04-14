package com.finndog.moogs_structures.platform;

import java.util.ServiceLoader;

public class Services {

    public static final IRegistryPlatform REGISTRY = load(IRegistryPlatform.class);

    public static <T> T load(Class<T> clazz) {
        return ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
    }
}
