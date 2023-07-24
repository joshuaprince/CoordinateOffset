package com.jtprince.coordinateoffset.apiexample;

import com.jtprince.coordinateoffset.CoordinateOffset;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.OffsetProvider;
import com.jtprince.coordinateoffset.OffsetProviderContext;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class MyCustomOffsetProvider extends OffsetProvider {
    public static final String className = "MyCustomOffsetProvider";
    private final int someParameter;

    public MyCustomOffsetProvider(String name, int someParameter) {
        super(name);
        this.someParameter = someParameter;
    }

    /*
     * Implement a "getOffset" method that takes in an offset provider context (containing the player the offset is for,
     * the world they are in, etc.), and returns a new Offset for that player.
     */
    @Override
    public @NotNull Offset getOffset(@NotNull OffsetProviderContext context) {
        // Match on elements of the Player.
        // NOTE: Don't use Player#getWorld or Player#getLocation here; use context.world() or context.playerLocation()
        //       instead!
        if (context.player().getName().contains("y")) {
            return Offset.ZERO;
        }

        // Match on the world this offset will be applied in.
        if (context.world().getEnvironment() == World.Environment.THE_END) {
            return new Offset(1600, -16); // Remember to use multiples of 16!
        }

        // Match on the location this player is.
        // NOTE: This is only the location they are at *now*, they could move around and not necessarily trigger a
        //       re-provide!
        if (context.playerLocation().getY() < someParameter) {
            return Offset.random(10000);
        }

        // Or, you could just use a static offset for everyone.
        return Offset.align(1601, -1599); // Rounds to the nearest multiples of 16
    }

    /*
     * Implement a "Factory" that converts a user's config.yml section to a Provider object. This example will expect
     * the following format:
     *
     * offsetProviders:
     *   customProvider:
     *     class: MyCustomOffsetProvider
     *     someParameter: 5
     *
     * Remember to register it in your plugin's onEnable!
     */
    public static class Factory implements OffsetProvider.ConfigurationFactory<OffsetProvider> {
        @Override
        public @NotNull MyCustomOffsetProvider createProvider(String name,
                                                              CoordinateOffset plugin,
                                                              ConfigurationSection providerConfig) throws IllegalArgumentException {
            int someParameter = providerConfig.getInt("someParameter");

            /* Perform validation if necessary. */
            if (someParameter == 0) throw new IllegalArgumentException("someParameter has to be nonzero!");

            /* Return an instance of your custom provider class. */
            return new MyCustomOffsetProvider(name, someParameter);
        }
    }
}
