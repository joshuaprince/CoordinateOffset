package com.jtprince.coordinateoffset.adapter;

import com.jtprince.coordinateoffset.OffsetHolder;

public interface OffsetSwapper {
    /**
     * Forcibly swap a player's next offset into their current offset in-place, sending any packets necessary to
     * simulate a teleport.
     *
     * <p>This may be called after {@link OffsetHolder#generateNextOffset} to apply an offset change immediately.</p>
     *
     * <p>This must only be called on the main server thread.</p>
     *
     * @param player Player to swap the offset for.
     */
    void forceOffsetSwap(OffsetPlayer player);
}
