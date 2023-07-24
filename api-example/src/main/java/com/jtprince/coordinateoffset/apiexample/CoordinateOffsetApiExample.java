package com.jtprince.coordinateoffset.apiexample;

import com.jtprince.coordinateoffset.CoordinateOffset;
import com.jtprince.coordinateoffset.Offset;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class CoordinateOffsetApiExample extends JavaPlugin implements Listener {
    private CoordinateOffset coordinateOffset;
    @Override
    public void onEnable() {
        coordinateOffset = CoordinateOffset.getInstance();

        getServer().getPluginManager().registerEvents(this, this);

        coordinateOffset.registerCustomProviderClass(MyCustomOffsetProvider.className, new MyCustomOffsetProvider.Factory());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        // Goal: Tell the player the "fake" coordinates (the ones they see) of the block they clicked on.
        Block block = event.getClickedBlock();
        if (block == null) return;

        Offset offset = coordinateOffset.getOffset(event.getPlayer());

        Location realBlockLocation = block.getLocation();
        Location blockLocationInPlayerSpace = offset.apply(realBlockLocation);

        event.getPlayer().sendMessage("You clicked a block at " + locationToString(blockLocationInPlayerSpace) + ".");
        event.getPlayer().sendMessage("(The block is really at " + locationToString(realBlockLocation) + ", but you shouldn't know that!)");
    }

    private String locationToString(Location l) {
        return "(" + l.getX() + ", " + l.getY() + ", " + l.getZ() + ")";
    }
}
