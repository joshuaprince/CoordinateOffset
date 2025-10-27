package com.jtprince.coordinateoffset.provider;

import com.jtprince.coordinateoffset.CoordinateOffsetCore;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.adapter.OffsetPlayer;
import com.jtprince.coordinateoffset.provider.util.CoordinateScaleUtils;
import com.jtprince.coordinateoffset.provider.util.ProviderOffsetStore;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.SequencedMap;

@NullMarked
public final class ConstantOffsetProvider extends CoreOffsetProvider {
    final Offset offset;
    private final @Nullable Boolean rememberChangesFromSetCommand;
    private final @Nullable ProviderOffsetStore offsetStore;

    ConstantOffsetProvider(String name, Offset offset, @Nullable Boolean rememberChangesFromSetCommand) {
        super(name);
        this.offset = offset;
        this.rememberChangesFromSetCommand = rememberChangesFromSetCommand;
        if (rememberChangesFromSetCommand != null && rememberChangesFromSetCommand) {
            this.offsetStore = new ProviderOffsetStore(
                CoordinateOffsetCore.get().getAdapter().getPersistenceAdapter(),
                name,
                null
            );
        } else {
            this.offsetStore = null;
        }
    }

    @Override
    public Offset provideOffset(OffsetProviderContext context) {
        Offset o = this.offset;

        if (offsetStore != null) {
            if (context.reason() == OffsetProviderContext.ProvideReason.COMMAND_REGENERATE) {
                offsetStore.clear(context.player().getUuid());
            }
            Offset storedOffset = offsetStore.get(context.player());
            if (storedOffset != null) {
                // User configured a remembered offset via a /set command for this player.
                if (CoordinateOffsetCore.get().getConfig().getVerbose()) {
                    CoordinateOffsetCore.get().getLogger().info("Provider \"" + name + "\": Using " + storedOffset +
                        " previously set via command for " + context.player().getName());
                }
                o = storedOffset;
            }
        }

        return CoordinateScaleUtils.scaleVerbosely(
            o,
            context.playerLocation().getWorld(),
            this,
            "constant"
        );
    }

    @Override
    public void onOffsetSetByCommand(OffsetPlayer target, Offset offset) {
        if (offsetStore != null) {
            if (CoordinateOffsetCore.get().getConfig().getVerbose()) {
                CoordinateOffsetCore.get().getLogger().info("Provider \"" + name + "\": Remembering " + offset +
                    "for " + target.getName());
            }
            offsetStore.put(target, offset);
        }
    }

    @Override
    public SequencedMap<String, ?> serialize() {
        SequencedMap<String, Object> map = new LinkedHashMap<>();
        map.put("offsetX", (long) offset.x());
        map.put("offsetZ", (long) offset.z());
        if (rememberChangesFromSetCommand != null) {
            map.put("rememberChangesFromSetCommand", rememberChangesFromSetCommand);
        }
        return map;
    }

    public static ConstantOffsetProvider deserialize(OffsetProviderConfig config) throws IllegalArgumentException {
        SequencedMap<String, Object> s = config.getConfigSection();

        if (!s.containsKey("offsetX") || !(s.get("offsetX") instanceof Number offsetXNum)) {
            throw new IllegalArgumentException("Provider \"" + config.getUserDefinedProviderName() +
                "\": Required key offsetX for ConstantOffsetProvider is missing or invalid.");
        }
        if (!s.containsKey("offsetZ") || !(s.get("offsetZ") instanceof Number offsetZNum)) {
            throw new IllegalArgumentException("Provider \"" + config.getUserDefinedProviderName() +
                "\": Required key offsetZ for ConstantOffsetProvider is missing or invalid.");
        }

        int offsetX = offsetXNum.intValue();
        int offsetZ = offsetZNum.intValue();

        if (Math.abs(offsetX) > OffsetProvider.OFFSET_MAX) {
            throw new IllegalArgumentException("Provider \"" + config.getUserDefinedProviderName() +
                "\": offsetX value " + offsetX + " is too large! (Max 30M)");
        }
        if (Math.abs(offsetZ) > OffsetProvider.OFFSET_MAX) {
            throw new IllegalArgumentException("Provider \"" + config.getUserDefinedProviderName() +
                "\": offsetZ value " + offsetZ + " is too large! (Max 30M)");
        }

        Boolean rememberChangesFromSetCommand = null;
        if (config.getConfigSection().containsKey("rememberChangesFromSetCommand")) {
            if (!((config.getConfigSection().get("rememberChangesFromSetCommand")) instanceof Boolean b)) {
                throw new IllegalArgumentException("Provider \"" + config.getUserDefinedProviderName() +
                    "\": rememberChangesFromSetCommand must be a boolean");
            }
            rememberChangesFromSetCommand = b;
        }

        return new ConstantOffsetProvider(
            config.getUserDefinedProviderName(),
            new Offset(offsetX, offsetZ),
            rememberChangesFromSetCommand
        );
    }

    @Override
    public String getMetricsClassName() {
        return "ConstantOffsetProvider";
    }

    @Override
    public String getMetricsDetails() {
        // No details reported for constant providers.
        return getMetricsClassName();
    }
}
