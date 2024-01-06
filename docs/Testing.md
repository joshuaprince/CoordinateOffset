Testing
=======
These are some specific test cases for Minecraft game mechanics that have historically been buggy or complex in
CoordinateOffset. I leave them here as notes to future developers who may wish to port the plugin to new versions,
so that they can hit the majority of possible issues before release. General guidelines for a new release:

* There should be no errors or warnings printed in the console from any of the tests.
* At all times, the coordinates shown for a player with an offset should properly reflect that offset.
* Enable "verbose" mode and disable "bypassByPermission" in the plugin configuration YAML.
* Validate at least a few cases on at least Spigot and Paper servers.
* If possible, use a packet sniffer like [SniffCraft](https://github.com/adepierre/SniffCraft),
  [Pakkit](https://github.com/Heath123/pakkit), or another one using
  [node-minecraft-protocol](https://github.com/PrismarineJS/node-minecraft-protocol) to make sure that no packets are
  leaking through unoffsetted.

Basic Tests
-----------
* Verify the plugin loads and prints an informational message with no errors or warnings.
* Join the server with a default plugin config and verify that offsets are working, chunks load, and commands
  (`/offset`) function.
* Verify that there are no errors in the console after connecting and waiting a few seconds for chunks to load.
* Verify that placing and breaking blocks behave as expected.
* Throw an item on the ground and pick it up, verifying that it behaves the same as Vanilla.
* Verify that some standard mobs are visible, and that you can:
  * Attack them
  * Hear sounds coming from them
  * Receive damage from them

Multi-world Tests
-----------------
* Build a Nether portal and enter it. Verify that "(player changed worlds)" is printed in the console debug message.
* Verify that on a default plugin configuration, the offset in the Nether is 1/8th of that in the Overworld.
* Go to The End (`/execute in minecraft:the_end run tp @p 0 100 0`).
  * Verify that there is an offset present in the End.
  * Verify that the Dragon appears to move naturally and connect to End Crystals.
  * Kill the Dragon (`/kill @e[type=minecraft:ender_dragon]`) and verify that the exit portal appears correctly.
  * Exit the End through the portal and verify that "(player changed worlds)" is printed in the console debug message.

Item and Block Tests
--------------------
These items and blocks have some kind of interactions that use packets that need to be offsetted.
* Chest
  * Verify that the chest appears to open and makes a sound, both to the opener and a second player.
  * Verify that the items in the chest appear instantly when the window opens.
* Command Block
  * Verify that you can set a simple command (like `say hello`) and activate it with a Redstone signal.
* Sign
  * Place a sign, set some text on it, and verify that the text is visible.
  * Log out and back in and verify that the text is still visible.
  * Edit the sign and verify that the edit goes through and persists after logging out and back in.
* Bed
  * Place a bed and verify that both halves of the bed are properly placed.
  * Place a bed such that each half is in a different chunk (F3+G), and verify that both halves are properly placed.
  * Enter the bed and verify that sleeping appears correctly (and that you don't fall into the void).
* Compass
  * Verify that it points to the world spawn point.
  * Verify that it spins in the Nether.
  * Verify that it still points to the world spawn point after returning from the Nether.
* Recovery Compass
  * Die and verify that it properly points to the place you died.
  * Change worlds and verify that it spins when in a different world from where you died.
  * Log out and back in in the world you died, and verify that it still points to the place you died.
  * Log in on an empty profile (that has never played or died before) and verify that the compass spins.
* Lodestone/Lodestone Compass
  * NOTE: These act strangely in Creative mode, so do these tests in Survival.
  * Verify that an attuned compass points to the Lodestone.
  * Log out and back in in the world with the Lodestone, and verify that it still points to the Lodestone.
  * Change worlds and verify that it spins when in a different world from the Lodestone.
* Sculk Sensor
  * Verify that you can activate it by moving around, it visibly gives off a redstone signal, and you can see the
    particle fly from yourself to the sensor.
  * Verify that you can trigger it more than once.
* Painting
  * Verify that you can place and break a painting.
* Minecarts/Boats
  * Verify that you can place, enter, and move these vehicles.
  * Verify that a second passenger in a boat moves along with the boat properly.
  * Verify that "vehicle moved too quickly" messages do not cause the player to be kicked.
    * Enter a boat
    * Cause the server to freeze (with a debugger breakpoint)
    * Move the boat ~100 blocks
    * Unfreeze the server and ensure the boat moves back to near where you started without getting kicked

Entity Tests
------------
* Experience Orbs
  * Throw an XP bottle and ensure that the XP orb shows up and moves towards you.
* Explosives
  * Blow up TNT and make sure it shows particles, makes sound, and destroys blocks that drop.
* Villager
  * Verify that trading works as expected and Villagers display particles after a successful trade.
  * Verify that villagers appear to enter beds properly at night.

Multiplayer Tests
-----------------
These tests require 2 accounts or offline mode. They all expect that each player has a different nonzero offset.
* Verify that players can damage each other, hear and see the damage, and take knockback.
* Verify that players can see and hear each other placing blocks.
* Verify that in Survival mode, block-breaking particles (cracks) appear for both players.
* Verify that players can see each other enter beds.

Command Tests
-------------
* Verify that you can properly face an entity with `/tp @p ~ ~ ~ facing entity <UUID>`.
* Verify that you can properly face a coordinate with `/tp @p ~ ~ ~ facing <x> <y> <z>`.
* Verify that /playsound works and players can hear the sounds.

World Border Tests
------------------
See https://github.com/joshuaprince/CoordinateOffset/wiki/Implications-and-Limitations#world-border - World border
obfuscation is complex.
NOTE: On Minecraft versions before 1.18, CoordinateOffset hides the border instead of obfuscating it.
* Run the following commands:
  * `/worldborder center 1000 1000`
  * `/worldborder set 3000`
* Teleport to `2499 ~ 2499` and ensure that the border is visible right next to you.
* If using a packet sniffer, move far away from the border and ensure that border packets indicate a center of (0, 0).
