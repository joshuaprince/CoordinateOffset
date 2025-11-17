WARNING: This changelog is automatically parsed by various Gradle release tasks. Maintain the format, especially the
"Supported Minecraft servers" line in each release — this line is parsed to set release versions on Hangar and Modrinth.

Do not add hard newlines, GitHub markdown renders them as newlines.

# v6.0.1
*Supported Minecraft servers: Paper 1.21.4-1.21.10*

This is a patch following v6.0.0 with minor fixes. Please see the full [v6.0.0 changelog](https://github.com/joshuaprince/CoordinateOffset/releases/tag/v6.0.0) and [v6 Upgrade Guide](https://github.com/joshuaprince/CoordinateOffset/wiki/v6-Upgrade-Guide) if upgrading from before v6.0.0.

- Fix version string in paper-plugin.yml being formatted incorrectly
- Fix config.yml parsing of floating-point numbers with no decimal point
- Allow zero values for `worldCoordinateScaleOverrides`
- Fix metrics reporting wrong statistic for `worldCoordinateScaleOverrides`
- API: Add `Offset#apply` method to all `Offset` types

# v6.0.0
*Supported Minecraft servers: Paper 1.21.4-1.21.10*

CoordinateOffset [5.0.0](https://github.com/joshuaprince/CoordinateOffset/releases/tag/v5.0.0) just came out last month,
but thanks to a breakthrough I made in **immediate offset changes**, it's already time for another major release.

"Immediate offset changes" means that the plugin no longer has to wait for the next opportune event to change a player's
offset. By simulating a teleport, the plugin can change a player's offset at any time. This allows for commands like
`/offset regenerate` and `/offset set`. It also allows safe offset changes when the player teleports any distance.

If you like reading, check out the fully documented list of changes in the
**[v6 Upgrade Guide](https://github.com/joshuaprince/CoordinateOffset/wiki/v6-Upgrade-Guide)**. You should especially
read this if you made any significant configuration changes or use the CoordinateOffset API. Otherwise, here's the
summary of everything new:

- **Regular backups are still mandatory.**
- Add `/offset regenerate` and `/offset set` commands.
- Add `PermissionOffsetProvider`.
- Add option to specify `offsetProviderOverrides` rules based on player name, world key, and world UUID. (Previously
  only player UUID and world name were supported.)
- Add `regenerateOnTeleport` and `minimumTeleportDistance` options to RandomOffsetProvider and
  ZeroAtLocationOffsetProvider. (Replaces `resetOnDistantTeleport` and `allowUnsafeResetOnDistantTeleport` settings)
- Rename `resetOn*` offset provider options to `regenerateOn*` to match the new command. (Configuration will
  automatically migrate and leave values unchanged.)
- Rename `persistent` RandomOffsetProvider option to `regenerateOnJoin`. (Configuration will automatically migrate
  and leave behavior unchanged; `false` and `true` are swapped automatically.)
- Remove `persistenceKey` Random offset provider option in favor of using provider name.
- Add `regenerateOnJoin` option to `ZeroAtLocationOffsetProvider`, allowing zero-at-location offsets to persist across
  joins and server restarts.
- Default all `regenerateOn*` options to false. (The `persistent` option previously defaulted to false; newly generated
  configurations now persist random offsets forever by default.)
- Redesign internal handling for scaling offsets between worlds.
  - Offsets now scale automatically based on world coordinate scale (e.g. nether scales down by 8).
  - Move `worldScaling` and `worldAlignment` options out of offset provider settings and into optional
    `worldCoordinateScaleOverrides` global configuration. (Not automatically migrated from the previous version)
- Add a default `offsetProviderOverrides` rule that disables offsets in the end. (Only for newly generated configs;
  can be removed if desired.)
- Automatically disable offsets for Bedrock players with no extra permissions/config required.
- Add `messages.yml` to allow customization/translation of most plugin messages.
- API Changes
  - Offsets returned from custom offset providers are now split into `ScalableOffset` and `FixedOffset` to give explicit
    control over how providers interact with world coordinate scaling.
  - Add new API endpoints to `regenerateOffset` and `setOffset` for immediate offset changes.
  - Add `onOffsetSetByCommand` interface to offset providers.

# v5.0.0
*Supported Minecraft servers: Paper 1.21.4-1.21.10*

I'm proud to announce CoordinateOffset's latest major release, **5.0.0**. This release overhauls lots of internal code
and restructures the entire project. This will facilitate some new features and platform support that I've been wanting
to add for a while. **Please report bugs on the [issue tracker](https://github.com/joshuaprince/CoordinateOffset/issues)
or in [Discord](https://discord.gg/V3xYtqU9JU).**

## Upgrading from CoordinateOffset 4.0

If you just want to upgrade, be aware of the following:

 - **Regular backups are mandatory.**
 - Minecraft versions **1.21.3 and below** and **all Spigot servers** are no longer supported. (Read below for more
   info)
 - **[PacketEvents](https://github.com/retrooper/packetevents/releases) is now required** as a dependent plugin.
 - Your CoordinateOffset `config.yml` will be stripped of all comments and reorganized on upgrade. Your existing
   configuration will automatically migrate to the new format (but also be backed up to `config.v4.old.yml`).
 - `/offsetreload` is now `/offset reload`. `/offset <player>` is now `/offset query <player>`.

## Known issues

- Certain particles (like ender dragon breath) cause a network protocol error. This is a PacketEvents bug:
  [packetevents#1373](https://github.com/retrooper/packetevents/issues/1373). Use a dev build of PacketEvents after
  they fix it (no CoordinateOffset update should be needed).
- `allowUnsafeResetOnDistantTel[PaperOffsetSwapper.java](paper/src/main/java/com/jtprince/coordinateoffset/paper/adapter/PaperOffsetSwapper.java)eport` is not working in 5.0.0. Set `unsafeResetOnDistantTeleport` in config.yml or use
  the latest
  [GitHub actions build](https://github.com/joshuaprince/CoordinateOffset/actions/runs/18637737025/artifacts/4312757481).

## Dropping support for <1.21.4 and Spigot

Minecraft versions below **1.21.4** and **all Spigot servers** are no longer supported. Paper
[hard-forked from Spigot](https://forums.papermc.io/threads/the-future-of-paper-hard-fork.1451/) almost a year ago,
and 1.21.9 was the first version where Paper's decisions split away from Spigot in a way that affects CoordinateOffset.
Dropping support let me delete a lot of legacy code, rewrite the command system in Brigadier, and spend less time
testing new releases.

Based on [bStats data](https://bstats.org/plugin/bukkit/CoordinateOffset/19988), at the time of writing, only 2 of 41
servers running CoordinateOffset are using Spigot. 4 of 41 servers are running a Minecraft version below 1.21.4. It
doesn't make sense to accommodate these servers with new plugin development. My apologies if you are affected, but
please stay on [v4.0.16](https://github.com/joshuaprince/CoordinateOffset/releases/tag/v4.0.16) if you need it.

## Full Changelog

- Support 1.21.9 and 1.21.10
- Convert plugin to a Paper plugin and increase API version to 1.21.4
- Stop shading PacketEvents; require PacketEvents as a dependency
- Replace configuration system with ConfigLib
  - Configuration is now automatically formatted and new options are added during plugin load
  - Comments and unexpected configuration will be deleted when the plugin loads
- Add new `obfuscateDebugPropertySubscriptions` config.yml setting
  - Minecraft 1.21.9+ has ["debug" properties](https://minecraft.wiki/w/Debug_property) that clients can enable. These
    properties show real coordinates inside entity brain data (such as bees and villagers).
  - The new setting disables debug properties for all clients with an offset applied.
  - This setting is enabled by default. Disabling it makes these debug flags work but reveals offsets to players.
- Rewrite `/offset` command
  - The output of all subcommands is now formatted more nicely
  - The command gives proper suggestions based on permissions
  - `/offsetreload` is now `/offset reload`
  - `/offset <player>` is now `/offset query <player>`
  - All permissions are unchanged
- Redesign API
  - Add a proper API published on Maven Central
  - The old API is no longer supported and will cause errors if any plugins still attempt to use it
  - See [API wiki page](https://github.com/joshuaprince/CoordinateOffset/wiki/API) for details
- Remove `debug` config.yml setting
  - This setting was not intended for production use and caused plugin errors when enabled.
- Various internal changes
  - Restructure code to be in `api`, `core`, and `paper` subprojects for increased modularization
  - Redesign offset change logic during join/respawn/world change to more accurately map to the game's protocol
  - Redesign offset storage logic to be more efficient with concurrency

# v4.0.16
- Fix console errors on player login for versions up to 1.21.4
- Rate-limit console spam during packet error conditions

# v4.0.15
- Support 1.21.8

# v4.0.14
- Support 1.21.7

# v4.0.13
- Support 1.21.6

# v4.0.12
- Fix ghost items appearing on the cursor when moving items with data components in an inventory with an offset applied
  in 1.21.5

# v4.0.11
- Fix underground rain and missing beacon beams in 1.21.5 caused by missing heightmaps

# v4.0.10
- Support 1.21.5
- Fix structure blocks being unusable when an offset is applied

# v4.0.9
- Fix chat validation error when plugin is installed in certain Geyser/offline-mode setups
- Update packetevents dependency

# v4.0.8
- Fix compatibility with GrimAC
- Update packetevents dependency

# v4.0.7
- Support 1.21.4

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
