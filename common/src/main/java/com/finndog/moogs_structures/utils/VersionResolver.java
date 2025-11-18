package com.finndog.moogs_structures.utils;

import com.mojang.serialization.DataResult;
import net.minecraft.SharedConstants;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * Utility methods for parsing version ranges and resolving the appropriate
 * resource locations for version-specific content.
 */
public final class VersionResolver {

    private static final VersionNumber CURRENT_VERSION = VersionNumber.parseInternal(SharedConstants.getCurrentVersion().getName());
    private static final String CURRENT_VERSION_STRING = CURRENT_VERSION.toString();

    private VersionResolver() {
    }

    public static VersionNumber getCurrentVersion() {
        return CURRENT_VERSION;
    }

    public static String getCurrentVersionString() {
        return CURRENT_VERSION_STRING;
    }

    public static DataResult<List<VersionEntry>> parseVersionMap(Map<String, ResourceLocation> raw) {
        List<VersionEntry> entries = new ArrayList<>();
        for (Map.Entry<String, ResourceLocation> entry : raw.entrySet()) {
            DataResult<VersionEntry> parsedEntry = parseRange(entry.getKey())
                    .map(range -> new VersionEntry(entry.getKey(), range, entry.getValue()));

            Optional<VersionEntry> result = parsedEntry.result();
            if (result.isEmpty()) {
                String errorMessage = parsedEntry.error().map(errorResult -> errorResult.message()).orElse("Unknown version range error");
                return DataResult.error(() -> errorMessage);
            }
            entries.add(result.get());
        }
        return DataResult.success(List.copyOf(entries));
    }

    public static DataResult<Map<String, ResourceLocation>> encodeVersionEntries(List<VersionEntry> entries) {
        LinkedHashMap<String, ResourceLocation> map = new LinkedHashMap<>();
        for (VersionEntry entry : entries) {
            map.put(entry.rawRange(), entry.location());
        }
        return DataResult.success(map);
    }

    public static Optional<VersionEntry> resolve(List<VersionEntry> entries, VersionNumber version) {
        for (VersionEntry entry : entries) {
            if (entry.range().contains(version)) {
                return Optional.of(entry);
            }
        }
        return Optional.empty();
    }

    public static DataResult<VersionRange> parseRange(String raw) {
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return DataResult.error(() -> "Version range cannot be empty");
        }

        String[] tokens = trimmed.split("-", -1);
        if (tokens.length == 1) {
            return parseVersionNumber(tokens[0])
                    .map(number -> new VersionRange(number, number));
        }

        if (tokens.length == 2) {
            if (tokens[0].isEmpty() || tokens[1].isEmpty()) {
                return DataResult.error(() -> "Version range '" + raw + "' must specify both minimum and maximum versions");
            }

            DataResult<VersionNumber> minResult = parseVersionNumber(tokens[0]);
            DataResult<VersionNumber> maxResult = parseVersionNumber(tokens[1]);

            Optional<VersionNumber> min = minResult.result();
            if (min.isEmpty()) {
                String errorMessage = minResult.error().map(errorResult -> errorResult.message())
                        .orElse("Failed to parse minimum version for range '" + raw + "'");
                return DataResult.error(() -> errorMessage);
            }

            Optional<VersionNumber> max = maxResult.result();
            if (max.isEmpty()) {
                String errorMessage = maxResult.error().map(errorResult -> errorResult.message())
                        .orElse("Failed to parse maximum version for range '" + raw + "'");
                return DataResult.error(() -> errorMessage);
            }

            if (min.get().compareTo(max.get()) > 0) {
                return DataResult.error(() -> "Version range '" + raw + "' has a minimum greater than its maximum");
            }

            return DataResult.success(new VersionRange(min.get(), max.get()));
        }

        return DataResult.error(() -> "Version range '" + raw + "' has too many '-' separators");
    }

    private static DataResult<VersionNumber> parseVersionNumber(String raw) {
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return DataResult.error(() -> "Version value cannot be empty");
        }

        String[] parts = trimmed.split("\\.");
        List<Integer> numbers = new ArrayList<>(parts.length);
        for (String part : parts) {
            if (part.isEmpty()) {
                return DataResult.error(() -> "Version '" + raw + "' contains empty components");
            }
            try {
                numbers.add(Integer.parseInt(part));
            } catch (NumberFormatException exception) {
                return DataResult.error(() -> "Version '" + raw + "' contains non-numeric component '" + part + "'");
            }
        }

        return DataResult.success(new VersionNumber(List.copyOf(numbers)));
    }

    public record VersionEntry(String rawRange, VersionRange range, ResourceLocation location) {
    }

    public record VersionRange(VersionNumber minInclusive, VersionNumber maxInclusive) {

        public VersionRange {
            Objects.requireNonNull(minInclusive, "minInclusive");
            Objects.requireNonNull(maxInclusive, "maxInclusive");
        }

        public boolean contains(VersionNumber version) {
            return version.compareTo(minInclusive) >= 0 && version.compareTo(maxInclusive) <= 0;
        }

        @Override
        public String toString() {
            return minInclusive + "-" + maxInclusive;
        }
    }

    public record VersionNumber(List<Integer> parts) implements Comparable<VersionNumber> {

        public VersionNumber {
            if (parts.isEmpty()) {
                throw new IllegalArgumentException("Version number must contain at least one component");
            }
            parts = List.copyOf(parts);
        }

        private static VersionNumber parseInternal(String value) {
            String[] tokens = value.split("\\.");
            List<Integer> numbers = new ArrayList<>(tokens.length);
            for (String token : tokens) {
                numbers.add(Integer.parseInt(token));
            }
            return new VersionNumber(List.copyOf(numbers));
        }

        @Override
        public int compareTo(VersionNumber other) {
            int maxLength = Math.max(this.parts.size(), other.parts.size());
            for (int index = 0; index < maxLength; index++) {
                int left = index < this.parts.size() ? this.parts.get(index) : 0;
                int right = index < other.parts.size() ? other.parts.get(index) : 0;
                if (left != right) {
                    return Integer.compare(left, right);
                }
            }
            return 0;
        }

        @Override
        public String toString() {
            StringJoiner joiner = new StringJoiner(".");
            for (Integer part : parts) {
                joiner.add(Integer.toString(part));
            }
            return joiner.toString();
        }
    }
}

