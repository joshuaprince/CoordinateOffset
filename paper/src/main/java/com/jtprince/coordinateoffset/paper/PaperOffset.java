package com.jtprince.coordinateoffset.paper;

import com.jeff_media.morepersistentdatatypes.DataType;
import com.jeff_media.morepersistentdatatypes.datatypes.GenericDataType;
import com.jtprince.coordinateoffset.Offset;
import org.bukkit.Location;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class PaperOffset {
    /**
     * Apply an Offset to a Bukkit Location, resulting in the Location that a player who has this Offset would see
     * if they were at that Location.
     *
     * <p>Care should be taken not to use the returned Location for anything internal to the server, such as getting
     * the Block at that Location. The returned Location is primarily intended to be sent to a Player who this Offset
     * is applied to, such as in a message.</p>
     *
     * @param realLocation A Location on the server, in real coordinate space.
     * @param offset The Offset to apply.
     * @return A new Location object that represents the coordinates that the player will see.
     */
    public static Location apply(Location realLocation, Offset offset) {
        return realLocation.clone().subtract(offset.x(), 0, offset.z());
    }

    /**
     * Apply the inverse of an Offset to a Bukkit Location, resulting in a real server Location.
     *
     * @param offsettedLocation An offsetted Location coming from a Player who has this offset.
     * @param offset The Offset to unapply.
     * @return A new Location object that represents the real Location for the server to use.
     */
    public static Location unapply(Location offsettedLocation, Offset offset) {
        return offsettedLocation.clone().add(offset.x(), 0, offset.z());
    }

    /**
     * Type for storing Offsets in Paper Persistent Data Containers (PDC).
     */
    public static final PersistentDataType<int[], Offset> PDT_TYPE =
        new GenericDataType<>(DataType.INTEGER_ARRAY.getPrimitiveType(), Offset.class, PaperOffset::fromPdt, PaperOffset::toPdt);

    private static Offset fromPdt(int[] arr) {
        return new Offset(arr[0], arr[1]);
    }

    private static int[] toPdt(Offset offset) {
        return new int[] { offset.x(), offset.z() };
    }
}
