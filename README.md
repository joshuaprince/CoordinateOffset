CoordinateOffset
================
**A Minecraft Paper plugin that configurably obfuscates players' coordinates.**

![Icon](.github/img/icon128.png)

[![GitHub Release](https://img.shields.io/github/v/release/joshuaprince/CoordinateOffset?link=https%3A%2F%2Fgithub.com%2Fjoshuaprince%2FCoordinateOffset%2Freleases)](https://github.com/joshuaprince/CoordinateOffset/releases)
[![Discord](https://img.shields.io/discord/1258675512583389236?logo=discord&logoColor=7389D8&label=discord&color=7389D8&link=https%3A%2F%2Fdiscord.gg%2FV3xYtqU9JU)](https://discord.gg/V3xYtqU9JU)
[![Ko-Fi Donations](https://img.shields.io/badge/ko--fi-jtprince-red?logo=ko-fi)](https://ko-fi.com/jtprince)

[![bStats Servers](https://img.shields.io/bstats/servers/19988?color=lightblue&link=https%3A%2F%2Fbstats.org%2Fplugin%2Fbukkit%2FCoordinateOffset%2F19988)](https://bstats.org/plugin/bukkit/CoordinateOffset/19988)
[![bStats Players](https://img.shields.io/bstats/players/19988?color=lightblue&link=https%3A%2F%2Fbstats.org%2Fplugin%2Fbukkit%2FCoordinateOffset%2F19988)](https://bstats.org/plugin/bukkit/CoordinateOffset/19988)
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/coordinateoffset?logo=modrinth&label=modrinth&link=https%3A%2F%2Fmodrinth.com%2Fplugin%2Fcoordinateoffset)](https://modrinth.com/plugin/coordinateoffset)
[![Spigot Downloads](https://img.shields.io/spiget/downloads/111292?logo=spigotmc&logoColor=yellow&label=spigot&color=yellow&link=https%3A%2F%2Fwww.spigotmc.org%2Fresources%2Fcoordinateoffset.111292%2F)](https://www.spigotmc.org/resources/coordinateoffset.111292/)
[![Hangar Downloads](https://img.shields.io/hangar/dt/CoordinateOffset?logo=paper&label=hangar&color=blue&link=https%3A%2F%2Fhangar.papermc.io%2Fjtchips%2FCoordinateOffset)](https://hangar.papermc.io/jtchips/CoordinateOffset)
[![CurseForge Downloads](https://img.shields.io/curseforge/dt/889789?logo=curseforge&logoColor=orange&label=bukkitdev&color=orange&link=https%3A%2F%2Fdev.bukkit.org%2Fprojects%2Fcoordinateoffset)](https://dev.bukkit.org/projects/coordinateoffset)

Minecraft offers a useful debug menu (F3) that allows anyone to easily see their coordinates in the world.
This makes it easy to save points of interest and share locations with friends (or enemies).

However, not all multiplayer servers want coordinates to be so easily accessible. `/gamerule reducedDebugInfo` can
administratively hide coordinates from the F3 menu, but it is trivial for a player to add a client-side mod that
shows them.

<p align="center">
<img src=".github/img/end.png" alt="Image demonstrating coordinate offsetting">
</p>

**CoordinateOffset** is a plugin for Paper-based Minecraft servers that modifies every coordinate in packets between the
server and client. The player still sees the exact same world they would normally see. But no matter which mods they
install, they cannot see their real coordinates.

Why?
----
This plugin isn't intended for all servers. Here are a few ideas that might make CoordinateOffset useful:
* **Prevent metagaming**: If you consider coordinate usage and sharing to be metagaming, this prevents it.
* **Amplify in-game items**: Coordinates no longer outclass compasses, lodestones, recovery compasses, and maps
  when those coordinates are inconsistent.
* **Prevent coordinate leaks**: If everyone sees different coordinates, players cannot derive each other's coordinates
  from an accidental leak in a screenshot.
* **Guard the world seed**: Any unknown offset makes seed-cracking tactics harder.
* **Center the origin**: Put the (0, 0) coordinate anywhere you'd like.

Features
--------
* Fully-configurable, flexible methods of determining how to apply offsets
* Generate offsets randomly for each player, or use fixed offsets for multiple players
* Match offsets to the player's position, so they see themselves near the world's origin
* Optionally regenerate offsets based on various player actions, including death, world change, teleport, and server
  join
* Regenerate and set offsets immediately with commands
* Configure different offsets per-player, per-world, and with permissions
* Automatic scaling for offsets to ensure coordinates still align when using a nether portal 
* Extensible API to flexibly get and set offsets
* Compatible with ViaVersion, ViaBackwards, and Velocity *(must be installed on Paper, not Velocity itself)*

Requirements and Installation
-----------------------------
* **An understanding of the 
  [implications of installing and incompatible plugins](https://github.com/joshuaprince/CoordinateOffset/wiki/Implications-and-Limitations)
  — this plugin WILL break things.**
* [Paper](https://papermc.io/) or a fork for Minecraft 1.21.4–1.21.10
* [PacketEvents](https://github.com/retrooper/packetevents/releases) (latest release or [dev build](https://ci.codemc.io/job/retrooper/job/packetevents/) for Spigot)

Some known **incompatible** plugins are: Most anticheats, Geyser, Distant Horizons.

After ensuring that you meet the requirements, just grab the latest
[release](https://github.com/joshuaprince/CoordinateOffset/releases/latest) and drop it in your server's `plugins`
folder. Then follow the steps below to configure how coordinates are shifted for each player.

Configuration
-------------
*Main article: [Configuration Guide](https://github.com/joshuaprince/CoordinateOffset/wiki/Configuration-Guide)*

The main configuration file is automatically generated after the first run at `plugins/CoordinateOffset/config.yml`.

```yaml
defaultOffsetProvider: random
```

The default configuration contains four predefined "offset providers". An "offset" refers to the amount that the
player's coordinates should appear to be shifted from their real location. Get started by picking a strategy that
matches the type of offsetting you're trying to achieve:
* `constant` - Specify the exact offset you want players to have.
* `disabled` - Players will see their real coordinates.
* `random` - Individually randomize each player's offset. Optionally re-roll offsets upon player actions.
* `zeroAtLocation` - Use an offset based on the player's starting location, so they see themselves near (0, 0).
  Optionally re-center offsets upon player actions.
* `permission` - Assign permissions to players, like `coordinateoffset.offset.160.-240`, to control their offset.

You can customize these providers further, use different providers for different players/worlds/groups, and define your
own providers. See the complete
[**Configuration Guide**](https://github.com/joshuaprince/CoordinateOffset/wiki/Configuration-Guide).

Commands and Permissions
------------------------

See the [Commands and Permissions Guide](https://github.com/joshuaprince/CoordinateOffset/wiki/Commands-and-Permissions)
for details.

API
---
You can use or extend CoordinateOffset in your own plugin by using the API. Please see the
[API Guide](https://github.com/joshuaprince/CoordinateOffset/wiki/API).

Support
-------
* Open an [issue on GitHub](https://github.com/joshuaprince/CoordinateOffset/issues) to report a bug or request a feature.
* Join the [Chips's Mods](https://discord.gg/V3xYtqU9JU) Discord for help.

Credits
-------
Special thanks to [Cavallium](https://github.com/cavallium) for developing
[**CoordinatesObfuscator**](https://github.com/cavallium/CoordinatesObfuscator), which CoordinateOffset is a fork of.
CoordinateOffset's packet translation logic was heavily modeled after the work done by CoordinatesObfuscator.

Libraries used:
* [PacketEvents](https://github.com/retrooper/packetevents)
* [ConfigLib](https://github.com/Exlll/ConfigLib)
* [bStats](https://bstats.org/) ([Statistics for this plugin](https://bstats.org/plugin/bukkit/CoordinateOffset/19988))
* [MorePersistentDataTypes](https://github.com/JEFF-Media-GbR/MorePersistentDataTypes)
