package com.jtprince.coordinateoffset.provider;

import com.jtprince.coordinateoffset.CoordinateOffsetCore;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.ScalableOffset;
import org.jspecify.annotations.NullMarked;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@NullMarked
public final class PermissionOffsetProvider extends CoreOffsetProvider {
    private final String prefix;
    private final Pattern pattern;

    PermissionOffsetProvider(
        String name,
        String prefix
    ) {
        super(name);
        this.prefix = prefix;
        this.pattern = Pattern.compile(Pattern.quote(prefix) + "\\.(-?\\d+)\\.(-?\\d+)");
    }

    @Override
    public Offset provideOffset(OffsetProviderContext context) {
        Set<String> allPerms = context.player().getAllPermissions();
        Set<String> matchingPerms = allPerms.stream()
            .filter(perm -> perm.startsWith(prefix))
            .collect(java.util.stream.Collectors.toSet());

        Set<ScalableOffset> offsets = new HashSet<>();
        for (String perm : matchingPerms) {
            try {
                Matcher matcher = pattern.matcher(perm);
                if (!matcher.matches()) {
                    throw new IllegalArgumentException("Pattern \"" + pattern.pattern() + "\" does not match permission \"" + perm + "\"");
                }
                int x = Integer.parseInt(matcher.group(1));
                int z = Integer.parseInt(matcher.group(2));
                ScalableOffset directOffset = Offset.scalable(x, z);
                ScalableOffset alignedOffset = Offset.align(x, z);
                if (!directOffset.equals(alignedOffset)) {
                    CoordinateOffsetCore.get().getLogger().warning("Provider \"" + name +
                        "\": Offset defined in " + context.player().getName() + "'s permission \"" + perm +
                        "\" is not aligned with " +
                        CoordinateOffsetCore.get().getConfig().getOffsetsAreMultiplesOfBlocks() +
                        " blocks; it will be rounded to " + alignedOffset + " to match the configured " +
                        "offsetsAreMultiplesOfBlocks. Change the permission to \"" +
                        prefix + "." + alignedOffset.x() + "." + alignedOffset.z() +
                        "\" to hide this warning.");
                }

                offsets.add(alignedOffset);

            } catch (Exception e) {
                String message = "Provider \"" + name + "\": Failed to decode permission \"" +
                    perm + "\" for player " + context.player().getName() + "; ensure all permissions starting with \"" +
                    prefix + "\" are formatted like \"" + prefix + ".x.z\".";
                if (CoordinateOffsetCore.get().isDebugEnabled()) {
                    new IllegalArgumentException(message, e).printStackTrace();
                } else {
                    CoordinateOffsetCore.get().getLogger().warning(message);
                }
            }
        }

        // If multiple offsets are defined for the same player, we have to decide on just 1.
        // The most important thing here is consistency, so arbitrarily, select the offset with components closest to
        //  negative infinity and warn the console that there's contention.
        List<ScalableOffset> sortedOffsets = offsets.stream()
            .sorted((o1, o2) -> {
                if (o1.x() == o2.x()) {
                    return Integer.compare(o1.z(), o2.z());
                } else {
                    return Integer.compare(o1.x(), o2.x());
                }
            })
            .toList();

        if (sortedOffsets.isEmpty()) {
            CoordinateOffsetCore.get().getLogger().warning("Provider \"" + name + "\": Player " +
                context.player().getName() + " has no permission matching \"" + prefix + ".x.z\". The player will " +
                "receive no offset. Give this player permission \"" + prefix + ".0.0\" to accept zero offset and " +
                "hide this warning.");
            return Offset.ZERO;
        } else if (sortedOffsets.size() == 1) {
            return sortedOffsets.getFirst();
        } else {
            CoordinateOffsetCore.get().getLogger().warning("Provider \"" + name + "\": Player " +
                context.player().getName() + " has permissions for multiple offsets: " +
                sortedOffsets.stream().map(ScalableOffset::toString).collect(Collectors.joining(", ")) +
                ". The first offset listed will be used. Remove the conflicting permissions of format \"" +
                prefix + ".x.z\" to hide this warning.");
            return sortedOffsets.getFirst();
        }
    }

    @Override
    public SequencedMap<String, ?> serialize() {
        SequencedMap<String, Object> map = new LinkedHashMap<>();
        map.put("prefix", prefix);
        return map;
    }

    public static PermissionOffsetProvider deserialize(OffsetProviderConfig config) throws IllegalArgumentException {
        SequencedMap<String, Object> s = config.getConfigSection();

        if (!s.containsKey("prefix")) {
            throw new IllegalArgumentException("Provider \"" + config.getUserDefinedProviderName() +
                ": Required key prefix for PermissionOffsetProvider is missing or invalid.");
        }
        // Trim off trailing periods - someone may assume they are necessary.
        String prefix = s.get("prefix").toString().replaceAll("\\.+$", "");

        return new PermissionOffsetProvider(config.getUserDefinedProviderName(), prefix);
    }

    @Override
    public String getMetricsClassName() {
        return "PermissionOffsetProvider";
    }

    @Override
    public String getMetricsDetails() {
        // No details reported for permission providers.
        return getMetricsClassName();
    }
}
