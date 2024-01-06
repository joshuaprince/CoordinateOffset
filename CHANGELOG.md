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
