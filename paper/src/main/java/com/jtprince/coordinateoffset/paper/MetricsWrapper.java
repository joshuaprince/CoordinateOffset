package com.jtprince.coordinateoffset.paper;

import com.jtprince.coordinateoffset.CoordinateOffsetCore;
import com.jtprince.coordinateoffset.config.CoordinateOffsetConfigBase;
import com.jtprince.coordinateoffset.provider.CoreOffsetProvider;
import com.jtprince.coordinateoffset.provider.OffsetProvider;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.DrilldownPie;
import org.bstats.charts.SimplePie;
import org.jspecify.annotations.NullMarked;

import java.util.HashMap;
import java.util.Map;

@NullMarked
public class MetricsWrapper {
    // https://bstats.org/plugin/bukkit/CoordinateOffset/19988
    private static final int BSTATS_PLUGIN_METRICS_ID = 19988;

    public static void reportMetrics(CoordinateOffsetPaperPlugin plugin) {
        Metrics metrics = new Metrics(plugin, BSTATS_PLUGIN_METRICS_ID);

        CoordinateOffsetCore core = CoordinateOffsetCore.get();

        metrics.addCustomChart(new DrilldownPie("default_offset_provider", () -> {
            Map<String, Map<String, Integer>> result = new HashMap<>();
            OffsetProvider defaultProvider = core.getProviderConfig().getDefaultOffsetProviderConfig();
            if (defaultProvider instanceof CoreOffsetProvider coreOffsetProvider) {
                // Only report full metrics for built-in ("core") offset providers.
                result.put(coreOffsetProvider.getMetricsClassName(), Map.of(coreOffsetProvider.getMetricsDetails(), 1));
            } else {
                // Intentionally obfuscate the name of any extensions made to CoordinateOffset.
                result.put("Custom Provider", Map.of("Unknown Offset Provider", 1));
            }
            return result;
        }));

        metrics.addCustomChart(new SimplePie("world_border_obfuscation", () ->
            enabledDisabledStr(core.getConfig().getObfuscateWorldBorder())));

        metrics.addCustomChart(new SimplePie("debug_packet_obfuscation", () ->
            enabledDisabledStr(core.getConfig().getObfuscateDebugPropertySubscriptions())));

        metrics.addCustomChart(new SimplePie("fix_collision", () -> {
            boolean enabled = false;
            StringBuilder sb = new StringBuilder();
            if (core.getConfig().getFixCollisionBamboo()) {
                enabled = true;
                sb.append("B");
            } else {
                sb.append("x");
            }
            if (core.getConfig().getFixCollisionDripstone()) {
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
            enabledDisabledStr(core.getConfig().getVerbose())));

        metrics.addCustomChart(new SimplePie("offset_provider_override_count", () ->
            String.valueOf(core.getProviderConfig().getOffsetProviderOverrides().size())));

        metrics.addCustomChart(new SimplePie("coord_scale_override_count", () ->
            String.valueOf(core.getConfig().getWorldCoordinateScaleOverrides().size())));

        metrics.addCustomChart(new SimplePie("offsets_are_multiples_of_blocks", () ->
            ((CoordinateOffsetConfigBase) core.getConfig()).offsetsAreMultiplesOfBlocks.getMetricsString()));
    }

    private static String enabledDisabledStr(boolean enabled) {
        return enabled ? "enabled" : "disabled";
    }
}
