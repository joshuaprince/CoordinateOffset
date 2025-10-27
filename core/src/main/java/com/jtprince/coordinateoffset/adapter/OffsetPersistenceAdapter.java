package com.jtprince.coordinateoffset.adapter;

import com.jtprince.coordinateoffset.Offset;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@NullMarked
public interface OffsetPersistenceAdapter {
    @Nullable Offset get(OffsetPlayer player, Key persistenceKey);
    void put(OffsetPlayer player, Key persistenceKey, Offset offset);
    void clear(UUID playerUuid, Key persistenceKey);

    /**
     * A key to index persistent player data, for example <code>provider.persistence.default</code>
     *
     * @param providerName User-defined name of the provider, for example <code>random</code>
     * @param persistenceKeyOverride An override that allows the user to specify a custom key for any persistent storage
     *                               on players. Takes precedence over the provider name.
     */
    record Key(
        String providerName,
        @Nullable String persistenceKeyOverride
    ) {
        public Key(String providerName) {
            this(providerName, null);
        }

        /**
         * Get the effective key to use for persistence. This may be the user-defined provider name (e.g. "random")
         * or the override set by the user (e.g. "default" for legacy configs).
         * @return The effective key to use for persistence.
         */
        public String getPersistenceKey() {
            return "provider." + (persistenceKeyOverride != null ? persistenceKeyOverride : providerName);
        }
    }
}
