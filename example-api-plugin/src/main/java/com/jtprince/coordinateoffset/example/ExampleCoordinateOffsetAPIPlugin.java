package com.jtprince.coordinateoffset.example;

import com.jtprince.coordinateoffset.FixedOffset;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.OffsetChange;
import com.jtprince.coordinateoffset.adapter.OffsetPlayer;
import com.jtprince.coordinateoffset.api.CoordinateOffset;
import com.jtprince.coordinateoffset.api.CoordinateOffsetAPI;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public class ExampleCoordinateOffsetAPIPlugin extends JavaPlugin implements Listener {

    @Nullable CoordinateOffsetAPI api;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        /*
         * If CoordinateOffset is listed in your [paper-]plugin.yml as a soft or optional dependency,
         *   use a try/catch block to check if the API is available.
         * If CoordinateOffset is listed as a regular or required dependency, try/catch isn't necessary (but
         *   still a good idea).
         */
        try {
            api = CoordinateOffset.api();
        } catch (NoClassDefFoundError e) {
            getLogger().warning("CoordinateOffset API not found. Proceeding without hooking into CoordinateOffset.");
        }

        if (api != null) {
            // Register a custom Offset Provider with CoordinateOffset to control how offsets are generated.
            api.registerOffsetProviderClass("ExampleOffsetProvider", ExampleOffsetProvider::deserialize);

            // Inspect CoordinateOffset's config file.
            boolean coVerbose = api.getConfig().getVerbose();
            boolean coPermBypass = api.getConfig().getBypassByPermission();
            getLogger().info("In CoordinateOffset config, verbose mode is " +
                (coVerbose ? "enabled" : "disabled") +
                " and permission bypass is " +
                (coPermBypass ? "enabled" : "disabled") +
                "."
            );
        }
    }

    @EventHandler
    public void onBlockPlaceLogOffset(BlockPlaceEvent event) {
        // Goal: When a player places a block, log the coordinates they see the block placed at.
        if (api == null) return;

        OffsetPlayer player = api.adaptPlayer(event.getPlayer()); // Adapt the Bukkit player to CO's generic type
        FixedOffset offset = api.getOffset(player);

        Location realBlockLocation = event.getBlock().getLocation();
        Location playerBlockLocation = offset.apply(realBlockLocation); // Apply the offset to the block's location

        getLogger().info(player.getName() + " placed " + event.getBlockPlaced().getType().key() + " at:");
        getLogger().info("    Real: " + formatBlockLocation(realBlockLocation)); // what the server sees
        getLogger().info("  Player: " + formatBlockLocation(playerBlockLocation)); // what the player sees
    }

    @EventHandler
    public void onConsumePoisonPotatoRegenerateOffset(PlayerItemConsumeEvent event) {
        // Goal: When a player eats a poisonous potato, call upon the configured offset provider to regenerate their
        //  offset. (With default config, this re-rolls the random offset via RandomOffsetProvider.)
        if (api == null) return;

        if (event.getItem().getType() != Material.POISONOUS_POTATO) return;

        OffsetPlayer player = api.adaptPlayer(event.getPlayer()); // Adapt the Bukkit player to CO's generic type
        OffsetChange result = api.regenerateOffset(player);

        if (result.offsetChanged()) {
            getLogger().info(player.getName() + " ate a poisonous potato and changed their offset from " +
                Objects.requireNonNull(result.previousOffsetData()).offset() + // Always non-null
                " to " + result.newOffsetData().offset());
        } else {
            getLogger().info(player.getName() + "'s offset was unchanged by eating a poisonous potato.");
        }
    }

    @EventHandler
    public void onConsumeGoldenCarrotSetZeroOffset(PlayerItemConsumeEvent event) {
        // Goal: When a player eats a golden carrot, set their offset to zero (so that they see the real coordinates
        //  of the world). WARNING: This effect wears off as soon as the player teleports, changes worlds, or relogs.
        //  Instead, use regenerateOffset with a custom offset provider if you want finer control over when the offset
        //  is cleared.
        if (api == null) return;

        if (event.getItem().getType() != Material.GOLDEN_CARROT) return;

        OffsetPlayer player = api.adaptPlayer(event.getPlayer()); // Adapt the Bukkit player to CO's generic type
        OffsetChange result = api.setOffset(player, Offset.ZERO);

        if (result.offsetChanged()) {
            getLogger().info(player.getName() + " ate a golden carrot and changed their offset from " +
                Objects.requireNonNull(result.previousOffsetData()).offset() + // Always non-null
                " to " + result.newOffsetData().offset());
        } else {
            getLogger().info(player.getName() + "'s offset was unchanged by eating a golden carrot.");
        }
    }

    private String formatBlockLocation(Location l) {
        return String.format("(%.0f, %.0f, %.0f)", l.getX(), l.getY(), l.getZ());
    }
}
