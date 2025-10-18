Offset Change Sequence: Paper
=============================
This document describes the sequence of events that occur in a Paper-like server when a player joins the server,
changes worlds, or respawns in Minecraft, and how the CoordinateOffset plugin handles these events to apply offsets
correctly.

Determining the Sequence with PacketEventSequencer
--------------------------------------------------
The offset change sequence involves two hooks: Bukkit Events and packets. Bukkit Events fire on the main server thread.
Packets are hooked on a Netty thread.

The exact ordering of events and packets is important. However, events and packets may occur concurrently. "Concurrent"
has a very specific meaning: the exact order of events and packets is sometimes **nondeterministic**.

Generally, packets are sent in a deterministic order. Events are also fired in a deterministic order. However, packets
and events may interleave.

CoordinateOffset includes a utility, PacketEventSequencer, that helps to characterize the sequence of events and
packets. It is only enabled in the current code by adding `-Dcoordinateoffset.debug=true` to the JVM arguments.
PacketEventSequencer logs when certain critical events and packets occur. On its own, this is useful for getting a
general understanding of the sequence, but logging alone cannot show concurrency patterns.

PacketEventSequencer can also introduce randomized thread delays. Increase `DELAY_MS_MAX` in the source file to
enable this feature (it is very disruptive). Thread delays make concurrent patterns more likely to be visible on
repeated runs. For example, in 1.21.9+, standard logging shows PlayerJoinEvent always fires before the packet 
JOIN_GAME. But randomized delays expose concurrency: by joining the server multiple times, the log order of these
events and packets change. We can drill even further into the sequence by adding thread-only breakpoints in
PlayerJoinEvent. Thread-only breakpoints introduce even more delay and show that more packets following JOIN_GAME
are _also_ concurrent with PlayerJoinEvent.

The sequence of packets and events (as it is currently understood) is documented below. This sequence may change in
future versions of Paper. Remember that within a column, the ordering is generally deterministic, but the ordering
between columns may be nondeterministic. Arrows indicate packets and events that are known to occur concurrently.
Packets and events without an arrow are assumed to occur in a deterministic order.

1.21.9-1.21.10:
```
On player join:
   Event                                       Packet
  ------------------------------              -------------------------
   PlayerSpawnLocationEvent*
   PlayerJoinEvent  ◄────── concurrent ─────┬► JOIN_GAME
                                            ├► PLAYER_POSITION_AND_LOOK
                                            ├► INITIALIZE_WORLD_BORDER
                                            └► SPAWN_POSITION
                                               UPDATE_VIEW_POSITION
                                               INITIALIZE_WORLD_BORDER
                                               SPAWN_POSITION

On world change:
   Event                                       Packet
  ------------------------------              -------------------------
   PlayerTeleportEvent
   PlayerChangedWorldEvent ◄── concurrent ─┬►  RESPAWN
                                           ├►  UNLOAD_CHUNK (1+)
                                           ├►  PLAYER_POSITION_AND_LOOK
                                           ├►  UPDATE_VIEW_POSITION
                                           ├►  INITIALIZE_WORLD_BORDER
                                           └►  SPAWN_POSITION

On respawn after death:
   Event                                       Packet
  ------------------------------              -------------------------
                                               UNLOAD_CHUNK (1+) (sent on death, before clicking "Respawn")
   PlayerRespawnEvent
   PlayerPostRespawnEvent  ◄── concurrent ─┬►  RESPAWN
                                           ├►  PLAYER_POSITION_AND_LOOK
                                           ├►  SPAWN_POSITION
                                           ├►  INITIALIZE_WORLD_BORDER
                                           ├►  SPAWN_POSITION
                                           └►  UPDATE_VIEW_POSITION
*Deprecated in 1.21.9.
```

General Strategy for Sequencing Offset Changes
----------------------------------------------
`OffsetHolder.java` contains the logic for changing offsets. This is the gist of it:
 - Track all offsets in a ConcurrentHashMap, providing safety between the main server thread and the Netty thread.
 - Keep two offsets for each player: "current" and "next".
 - When an event like PlayerTeleportEvent occurs (which may trigger an offset change), call Offset Providers and store
   the result in the player's "next" offset.
 - Packets continue using the player's "current" offset.
 - When we see a specific packet, usually RESPAWN, swap the player's "next" offset into "current".

This strategy ensures that _packets_ and the Netty thread are the impetus for offset changes. It gives us the context
we need on the main thread to generate the offset. But it ensures that offsets always change at a specific point in the
packet sequence.

Player Join
-----------
When a player joins the server, we have to generate an offset for them. We can't generate an offset too early because
the context needed (e.g. the player's location and permissions) isn't available yet. We can't generate an offset too
late because the very first "PLAY" packet, JOIN_GAME, contains coordinates that must be offset.

Prior to 1.21.9, the earliest hook we could use to generate an offset was PlayerSpawnLocationEvent. This event was
fired _before_ sending JOIN_GAME, but after player data was available. Therefore, these versions use
PlayerSpawnLocationEvent to generate an offset for joining players.

In 1.21.9, Paper changed two things:
 - PlayerSpawnLocationEvent is now deprecated. The replacement, AsyncPlayerSpawnLocationEvent, is not a suitable hook
   because it contains no Player object or any player data beyond a UUID.
 - PlayerJoinEvent is now fired _concurrently_ with sending a JOIN_GAME packet. (Previously, JOIN_GAME always came
   before PlayerJoinEvent.) Adding breakpoints shows that the server thread receives a PlayerJoinEvent and the Netty
   thread sends a JOIN_GAME packet at the same time.

The second change is actually a good solution to the first. However, it requires a hack to work around the concurrency.
The strategy 1.21.9+ use is to use PlayerJoinEvent to generate an offset and to **block** the Netty thread from sending
a JOIN_GAME packet until an offset is generated.

Respawn and World Change
------------------------
Respawns and world changes use the general strategy described above. The respective event generates a new offset.
Then, both respawns and world changes send a RESPAWN packet, which triggers the offset swap.
