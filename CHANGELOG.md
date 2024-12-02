# v4.1.0-alpha1
- Add basic PlaceholderAPI support
    - Placeholders defined by CoordinateOffset:
        - `%coordinateoffset%`: Resolves to a player's current offset as a comma-separated string, e.g. "1600,-320"
        - `%coordinateoffset_x%`: Resolves to the X-component of a player's current offset, e.g. "1600"
        - `%coordinateoffset_z%`: Resolves to the Z-component of a player's current offset, e.g. "-320"
    - Add PlaceholderOffsetProvider class to set offsets based on an external placeholder
        - Supports `placeholderXZ` required key to define the placeholder string (must resolve to a comma-separated
          pair of integers which are both divisible by 16)
        - Supports `worldScaling` key with identical syntax to ConstantOffsetProvider
- Known limitations
    - Things WILL break if any player on the server does not have proper placeholder values set when resolving the
      `placeholderXZ` value. A proper fallback mechanism is necessary for a non-alpha release.
    - Things WILL break if a PlaceholderOffsetProvider attempts to resolve an offset which is not divisible by 16.
      Things will also break if a `worldScaling` setting divides the resolved offset such that the result is not
      divisible by 16. **Use at your own risk** and ensure that all offsets provided by a placeholder are divisible by
      (16 * 8) for the default worldScaling configuration.

# v4.0.6
- Fix packet decoding error and kick caused by potions in inventory or in a container
- Update packetevents dependency

# v4.0.5
- Support 1.21.2 and 1.21.3

# v4.0.4
- Update packetevents dependency
- Make "version not found" joke in the changelog

# v4.0.3
- Support 1.21.1
- Fix "Unknown player of Offset lookup" errors related to logging in from another location

# v4.0.2
- Fix "Unknown player for Offset lookup" errors printed occasionally when players disconnect
- Fix Random offsets occasionally resetting when changing worlds or dying even when the option is disabled
- Update packetevents dependency

# v4.0.1
- Fix NBT errors causing world to sometimes not load when joining

# v4.0.0
- Support 1.21
- Fix movement near bamboo and dripstone when an offset is applied
    - **NOTE**: Servers migrating from v3 should add the [`fixCollision`](https://github.com/joshuaprince/CoordinateOffset/commit/226586c8412c142bc1b6c0a254013533526fc4f7#diff-58cdd3d308ccba6c594e040ff9c065bb11eeb6e30f35ba87694ea45d5ae6096c)
      section to their CoordinateOffset config.yml. However, this fix is enabled by default even if these lines are
      omitted.
- Remove separate PacketEvents dependency
    - PacketEvents is now shaded into CoordinateOffset, so it is not necessary to install as a separate plugin.
- Several fixes to 1.20.6 bugs present in PacketEvents v2.3.0
    - Fix players being kicked while near a horse or wolf that is wearing armor ([packetevents#827](https://github.com/retrooper/packetevents/pull/827))
    - Fix players respawning instantly after dying ([packetevents#816](https://github.com/retrooper/packetevents/issues/816))
    - Fix treasure maps with icons kicking players ([packetevents#811](https://github.com/retrooper/packetevents/issues/810))
    - Fix banners with patterns kicking players ([packetevents#772](https://github.com/retrooper/packetevents/issues/772))
    - Fix decorated pots in an inventory kicking players ([packetevents#768](https://github.com/retrooper/packetevents/issues/768))

# v3.1.0
- Support 1.20.6 (requires PacketEvents [dev build](https://ci.codemc.io/job/retrooper/job/packetevents/) #397+)
- Improve exception handling and debuggability
- Drop CommandAPI dependency; rewrite commands in standard Bukkit API

# v3.0.2
- Fixes for lodestone compasses
    - Fix Creative players seeing lodestone compasses pointing the wrong direction
    - Fix lodestone compasses on the ground or in other players' hands pointing the wrong direction

# v3.0.1
- Fix players being kicked when making sounds near a Warden

# v3.0.0 (beta)
- Plugin is now compatible with: (in beta; please report issues)
    - ViaVersion
    - ViaBackwards
    - Model Engine
- Plugin is compatible with Geyser (in alpha; not recommended for production)
    - With Geyser installed as a plugin, movement is buggy because Geyser's collision-fixing code processes real
      coordinates, while its translation layer deals with shifted coordinates.
    - If possible, it is currently recommended to use Geyser standalone so that the collision-fixing code is disabled.
- Rewrite packet offsetting logic with [PacketEvents](https://github.com/retrooper/packetevents) library
    - Plugin now depends on PacketEvents (v2), and no longer depends on ProtocolLib
- Fix exceptions and improperly offsetted coordinates when a player logs in from another location

# v2.3.2
- Support 1.20.4 (requires ProtocolLib dev build #676+)

# v2.3.1
- Remove broken shortened links in config.yml comment

# v2.3.0
- Backport plugin support to Spigot/Paper 1.17.1, 1.18.2, and 1.19 through 1.19.3 (plugin now supports all versions from 1.17.1 through 1.20.2)
- Mark `resetOnDistantTeleport` as unsafe and require opt-in ([more details](https://github.com/joshuaprince/CoordinateOffset/wiki/resetOnDistantTeleport))
- Add `worldScaling` option to ConstantOffsetProvider
- Fix "facing" packets (e.g. `/tp ~ ~ ~ facing 0 0 0`) not being properly offsetted, causing the player to face the wrong direction
- Fix player getting kicked when their vehicle moves too quickly

# v2.2.0
- Support 1.20.2 (requires ProtocolLib dev build #669)
- Rewrite majority of packet translation logic to support multiple protocol versions
- Add bStats anonymous plugin metrics
- Fix excessive world border packets while border is obfuscated

# v2.1.3
- Fix login error on BungeeCord/Waterfall (#5)

# v2.1.2
- Fix glitchy behavior when multiple players are logged into the server with offsets (#4)
    - Entities becoming invisible
    - Multi-blocks (beds, doors) failing to place properly
    - Beds appearing to teleport players into the void

# v2.1.1
* Improve `resetOnDistantTeleport` in providers to use world view distance to determine when a teleport is "distant"
    * Previously: Teleports over a distance of 2050+ blocks could trigger a re-roll (random providers) or re-center (zero-at-location providers)
    * Now: Teleports over a distance of `(2 * (world view distance + 1 chunk))` or further can trigger resets
    * On Paper servers, relative teleports (e.g. `/tp ~500 ~ ~500`) will no longer trigger a reset
* Improve API exposure for dependent plugins
* Support ProtocolLib 5.1.0

# v2.1.0
- Support 1.19.4
- Make world borders visible while an offset is applied, improve border handling and obfuscation
- Expose and document API for other plugins to get and set offsets

# v2.0.0
Initial release of CoordinateOffset.

Changelog from upstream:

- Rewrite all logic for determining offsets
- Expose offset configuration per player/world/permission through "[providers](https://github.com/joshuaprince/CoordinateOffset/wiki/Configuration-Guide)"
- Add `/offset` command (with permission) to query players' current offsets
- Fix death compasses not pointing to their correct location
