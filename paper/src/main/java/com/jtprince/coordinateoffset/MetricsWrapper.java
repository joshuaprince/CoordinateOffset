package com.jtprince.coordinateoffset;

import com.jtprince.coordinateoffset.provider.ConstantOffsetProvider;
import com.jtprince.coordinateoffset.provider.RandomOffsetProvider;
import com.jtprince.coordinateoffset.provider.ZeroAtLocationOffsetProvider;
import com.jtprince.coordinateoffset.provider.util.ResetConfig;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.DrilldownPie;
import org.bstats.charts.SimplePie;

import java.util.HashMap;
import java.util.Map;

public class MetricsWrapper {
    // https://bstats.org/plugin/bukkit/CoordinateOffset/19988
    private static final int BSTATS_PLUGIN_METRICS_ID = 19988;

    public static void reportMetrics(CoordinateOffset plugin) {
        Metrics metrics = new Metrics(plugin, BSTATS_PLUGIN_METRICS_ID);

        metrics.addCustomChart(new DrilldownPie("default_offset_provider", () -> {
            Map<String, Map<String, Integer>> result = new HashMap<>();
            OffsetProvider defaultProvider = plugin.getOffsetProviderManager().getDefaultProvider();
            if (defaultProvider instanceof ConstantOffsetProvider) {
                result.put("ConstantOffsetProvider", Map.of("ConstantOffsetProvider", 1));
            } else if (defaultProvider instanceof RandomOffsetProvider randomOffsetProvider) {
                StringBuilder sb = new StringBuilder();
                sb.append(randomOffsetProvider.isPersistent() ? "Persistent" : "Not Persistent");

                sb.append(" | Reset ");
                ResetConfig rc = randomOffsetProvider.getResetConfig();
                sb.append(rc.resetOn(OffsetProviderContext.ProvideReason.DEATH_RESPAWN) ? "D" : "x");
                sb.append(rc.resetOn(OffsetProviderContext.ProvideReason.WORLD_CHANGE) ? "W" : "x");
                sb.append(rc.resetOn(OffsetProviderContext.ProvideReason.DISTANT_TELEPORT) ? "T" : "x");

                result.put("RandomOffsetProvider", Map.of(sb.toString(), 1));
            } else if (defaultProvider instanceof ZeroAtLocationOffsetProvider zeroAtLocationOffsetProvider) {
                StringBuilder sb = new StringBuilder();
                sb.append("Reset ");
                ResetConfig rc = zeroAtLocationOffsetProvider.getResetConfig();
                sb.append(rc.resetOn(OffsetProviderContext.ProvideReason.DEATH_RESPAWN) ? "D" : "x");
                sb.append(rc.resetOn(OffsetProviderContext.ProvideReason.WORLD_CHANGE) ? "W" : "x");
                sb.append(rc.resetOn(OffsetProviderContext.ProvideReason.DISTANT_TELEPORT) ? "T" : "x");

                result.put("ZeroAtLocationOffsetProvider", Map.of(sb.toString(), 1));
            } else {
                // Intentionally obfuscate the name of any extensions made to CoordinateOffset.
                result.put("Custom Provider", Map.of("Unknown Offset Provider", 1));
            }
            return result;
        }));

        metrics.addCustomChart(new SimplePie("world_border_obfuscation", () ->
            enabledDisabledStr(plugin.getWorldBorderObfuscator().enableObfuscation())));

        metrics.addCustomChart(new SimplePie("unsafe_reset_on_teleport", () ->
            enabledDisabledStr(plugin.isUnsafeResetOnTeleportEnabled())));

        metrics.addCustomChart(new SimplePie("fix_collision", () -> {
            boolean enabled = false;
            StringBuilder sb = new StringBuilder();
            if (plugin.getConfig().getBoolean("fixCollision.bamboo", true)) {
                enabled = true;
                sb.append("B");
            } else {
                sb.append("x");
            }
            if (plugin.getConfig().getBoolean("fixCollision.dripstone", true)) {
                enabled = true;
                sb.append("D");
            } else {
                sb.append("x");
            }
            if (enabled) {
                return "enabled (" + sb + ")";
            } else {
                return "disabled";
            }
        }));

        metrics.addCustomChart(new SimplePie("verbose", () ->
            enabledDisabledStr(plugin.isVerboseLoggingEnabled())));

        metrics.addCustomChart(new SimplePie("offset_provider_override_count", () ->
            String.valueOf(plugin.getOffsetProviderManager().getLoadedOverrideCount())));
    }

    private static String enabledDisabledStr(boolean enabled) {
        return enabled ? "enabled" : "disabled";
    }
}
