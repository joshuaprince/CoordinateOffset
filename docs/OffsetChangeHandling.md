Offset Change Handling
=====================
The logic to properly obfuscate packets during offset changes is complex. Specifically, offset changes typically
happen right as the player changes world or joins the server. It is important to be careful with packets during the
following events:

* Player Login/Join
* Player World Change (Nether portals, End portals, Teleportation commands)
* Player Respawns

This document hopes to describe all the gotchas surrounding these events.

Event and Packet Ordering
-------------------------
Most of the complexity comes from the fact that Bukkit generates events and ProtocolLib generates packets in a fixed
but non-obvious order. The complete order is documented at the bottom of this file. In summary, this is the order these
gameplay events tend to fire the relevant Bukkit events and packets. All-caps are packets, "..." implies many packets
of that type.

```
Player Join:
    PlayerLoginEvent -> PlayerSpawnLocationEvent -> LOGIN -> POSITION -> PlayerJoinEvent -> INITIALIZE_BORDER -> MAP_CHUNK...

World Change (all portals EXCEPT the End Exit portal):
    PlayerTeleportEvent -> RESPAWN ->   UNLOAD_CHUNK... ->   POSITION ->                    INITIALIZE_BORDER -> MAP_CHUNK...

Respawn (on clicking the "Respawn" button on the death screen):
    PlayerRespawnEvent ->  RESPAWN ->                        POSITION ->                    INITIALIZE_BORDER -> MAP_CHUNK...

End Exit Portal (on completing the credits or hitting Esc):
    PlayerRespawnEvent ->  RESPAWN ->                        POSITION ->                    INITIALIZE_BORDER -> MAP_CHUNK...

Teleportation within the same world:
    PlayerTeleportEvent ->                                   POSITION ->           UNLOAD_CHUNK... ->            MAP_CHUNK...
```

Every packet in this diagram needs to have an offset applied to it. However, offsets can be different for different
worlds! That creates the following problems:
* Which event/packet should cause a newly-needed offset to be generated?
* What happens if one packet needs the "new" offset, but a subsequent packet needs the "old" one?
  * Example: World changes need to offset the RESPAWN packet with an offset from the world the player is going *to*, but
    the next UNLOAD_CHUNK packets still need to reference the offset they are coming *from*.

The solutions are built primarily in `java/com/jtprince/coordinateoffset/PlayerOffsetsManager.java`. This is what we do:
* Cache each player's last-known offset for each world, in `playerOffsets`.
* Store *two* trackers for which world each player is in.
  * The player's "Positioned" world is the primary way to keep track of which world a player is "in". It only changes
    when we send a POSITION packet, which (as shown in the chart above) comes very consistently before border and
    chunk packets in the new world.
  * The player's "Lookahead" world is an "early preview" into which world the player is moving to. We set this as soon
    as possible in the process: during the PlayerRespawnEvent or PlayerTeleportEvent, which is also the time at which
    this new offset is generated.

To rephrase, this is the general order of operations:
* Generate a new offset as early as possible, during PlayerSpawnLocationEvent, PlayerTeleportEvent, or 
  PlayerRespawnEvent. Put that offset into a cache indexed by the world that they're going *to*.
* Continue applying the old offset unless a packet explicitly asks for the world of the new offset.
* When we see a POSITION packet, only then do we update the implicit offset that gets applied to all packets.

Complete Notes
--------------
Different server implementations and versions seem to behave slightly differently. Here are some notes about how they
work.

```
1.20.2:
    Join:
        EVENT:  PlayerLoginEvent
        EVENT:  PlayerSpawnLocationEvent
        PACKET: LOGIN
        PACKET: POSITION
        EVENT:  PlayerJoinEvent
        PACKET: INITIALIZE_BORDER
        PACKET: SPAWN_POSITION
        PACKET: MAP_CHUNK
    World change (Nether portal, /execute in tp):
        EVENT:  PlayerTeleportEvent WORLD_CHANGE
        PACKET: RESPAWN
        PACKET: UNLOAD_CHUNK (Paper only, inverted with PACKET RESPAWN when using commands)
        PACKET: POSITION
        PACKET: SPAWN_POSITION (only with commands, skipped through portal)
        PACKET: INITIALIZE_BORDER
        PACKET: SPAWN_POSITION
        PACKET: MAP_CHUNK
    Death respawn (both across worlds and same world):
        PACKET: UNLOAD_CHUNK (Paper only, comes BEFORE clicking "Respawn")
        EVENT:  PlayerRespawnEvent reason=DEATH_RESPAWN
        PACKET: RESPAWN
        PACKET: POSITION
        PACKET: SPAWN_POSITION
        PACKET: INITIALIZE_BORDER
        PACKET: SPAWN_POSITION
        PACKET: POSITION (again only on Paper)
        PACKET: MAP_CHUNK
    End Exit Respawn:
        PACKET: UNLOAD_CHUNK
        EVENT:  PlayerRespawnEvent reason=WORLD_CHANGE
        PACKET: RESPAWN
        PACKET: POSITION
        PACKET: SPAWN_POSITION
        PACKET: INITIALIZE_BORDER
        PACKET: SPAWN_POSITION
        PACKET: POSITION (again) <-| Interleaved on Spigot!
        PACKET: MAP_CHUNK        <-| Don't rely on this order!
    Teleport (End Gateway, /tp same world):
        EVENT:  PlayerTeleportEvent DISTANT_TELEPORT
        PACKET: POSITION
        PACKET: UNLOAD_CHUNK (both Paper and Spigot)
        PACKET: MAP_CHUNK
1.20.1:
    Join:
        SAME AS 1.20.2
    World change (Nether portal, /execute in tp):
        EVENT:  PlayerTeleportEvent WORLD_CHANGE
        PACKET: RESPAWN
    --> (no unload packets)
        PACKET: POSITION
    --> PACKET: SPAWN_POSITION (only with commands)
    ==> PACKET: INITIALIZE_BORDER (!!happens AFTER MAP_CHUNK on Spigot portal only)
    ==> PACKET: SPAWN_POSITION    (!!happens AFTER MAP_CHUNK on Spigot portal only)
    --> PACKET: POSITION (again on Spigot commands only)
        PACKET: MAP_CHUNK
    Death respawn (both across worlds and same world):
    --> (no unload packets)
        EVENT:  PlayerRespawnEvent reason=DEATH_RESPAWN
        PACKET: RESPAWN
        PACKET: POSITION
        PACKET: SPAWN_POSITION
        PACKET: INITIALIZE_BORDER
        PACKET: SPAWN_POSITION
    --> (no position packet again)
        PACKET: MAP_CHUNK
    End Exit Respawn:
    --> (no unload packets)
        EVENT:  PlayerRespawnEvent reason=WORLD_CHANGE
        PACKET: RESPAWN
        PACKET: POSITION
        PACKET: SPAWN_POSITION
        PACKET: INITIALIZE_BORDER
        PACKET: SPAWN_POSITION
    --> (no position packet again)
        PACKET: MAP_CHUNK
    Teleport (End Gateway, /tp same world):
        SAME AS 1.20.2
```
