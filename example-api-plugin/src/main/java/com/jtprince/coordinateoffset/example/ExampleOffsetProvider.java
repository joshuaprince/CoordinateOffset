package com.jtprince.coordinateoffset.example;

import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.provider.OffsetProvider;
import com.jtprince.coordinateoffset.provider.OffsetProviderConfig;
import com.jtprince.coordinateoffset.provider.OffsetProviderContext;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.SequencedMap;

public class ExampleOffsetProvider extends OffsetProvider {
    private final int myScaleSetting;
    @Nullable private final String myStringSetting;

    public ExampleOffsetProvider(String userDefinedProviderName, int myScaleSetting, @Nullable String myStringSetting) {
        // `userDefinedProviderName` is a key under `offsetProviders` in CoordinateOffset config.yml
        super(userDefinedProviderName);
        this.myScaleSetting = myScaleSetting;
        this.myStringSetting = myStringSetting;
    }

    @Override
    public @NotNull Offset provideOffset(@NotNull OffsetProviderContext context) {
        /*
         * Implement your own logic to determine what Offset players should have. Some examples are provided below.
         *
         * Just remember to align all offsets to multiples of 16!
         */

        // If the player will be in any Nether dimension, return an Offset of 2000 blocks in each direction.
        // Always check the world provided in the context instead of Player#getWorld. The offset might be for
        //   a world the player is not in yet.
        World world = Bukkit.getWorld(context.worldName());
        if (world != null && world.getEnvironment() == World.Environment.NETHER) {
            return new Offset(2000, 2000);
        }

        // If the player has permission "example.offset.randomized", return a random Offset.
        if (context.player().hasPermission("example.offset.randomized")) {
            return Offset.random(10000);
        }

        // Cast the provided Player object to a Bukkit Player to check more conditions.
        // Never use Player#getWorld or Player#getLocation here! Use context.world() or context.playerLocation() instead!
        org.bukkit.entity.Player player = (org.bukkit.entity.Player) context.player().getPlatformPlayerObject();
        if (player.hasSeenWinScreen()) {
            return Offset.ZERO;
        }

        // Otherwise, return a fixed Offset of 16,000 blocks in each direction.
        // Use Offset.align() if you're not sure the numbers you're using are divisible by 16.
        return Offset.align(16001 * myScaleSetting, -15999 * myScaleSetting);
    }

    public static ExampleOffsetProvider deserialize(@NotNull OffsetProviderConfig config) throws IllegalArgumentException {
        // Write custom logic to get settings from the CoordinateOffset config.yml when a user configures this provider.
        // This example shows how to get an integer and String value from the provider config. The integer is required.
        // More complex examples are available in CoordinateOffset's core providers, such as RandomOffsetProvider.
        Object scale = config.getConfigSection().get("myScaleSetting");
        if (!(scale instanceof Number scaleNum)) { // Always use instanceof(Number), not Integer. Numbers may be Longs.
            throw new IllegalArgumentException("Provider \"" + config.getUserDefinedProviderName() +
                ": Required key `myScaleSetting` for ExampleOffsetProvider is missing or invalid.");
        }

        Object stringSettingObj = config.getConfigSection().get("myStringSetting");
        String stringSetting = null;
        if (stringSettingObj != null) {
            // Not required, but validate if provided
            if (!(stringSettingObj instanceof String)) {
                throw new IllegalArgumentException("Provider \"" + config.getUserDefinedProviderName() +
                    "\": Optional key `myStringSetting` for ExampleOffsetProvider is not a string.");
            }
            stringSetting = (String) stringSettingObj;
        }

        // Always return an instance using the user-defined provider name.
        return new ExampleOffsetProvider(config.getUserDefinedProviderName(), scaleNum.intValue(), stringSetting);
    }

    @Override
    public @NotNull SequencedMap<String, ?> serialize() {
        // You must provide logic to write configuration back to the CoordinateOffset config.yml.
        // Serialization logic must exactly match the logic in deserialize(), otherwise users' configurations will
        //   be unexpectedly changed when the plugin loads.
        SequencedMap<String, Object> map = new LinkedHashMap<>();
        // NOTE: Always serialize integers as Longs! The serializer will give an error if you provide an Integer.
        map.put("myScaleSetting", (long) myScaleSetting);
        if (myStringSetting != null) {
            map.put("myStringSetting", myStringSetting);
        }
        return map;
    }
}
