Updating
========

This is a guide for porting CoordinateOffset to a new Minecraft version.

This plugin is heavily dependent on the Minecraft protocol and its details. Luckily, PacketEvents abstracts away most of
the changes made to the protocol in each new Minecraft version. The important changes to be made in CoordinateOffset
are:
* When a new packet is added to support a new feature
* When an existing packet involving coordinates is changed

All interaction with PacketEvents is contained within the `core` module in the package
`com.jtprince.coordinateoffset.offsetter`. Logic for how to apply an offset to each packet type are contained within
the subpackages `client` and `server` for packets sent by the client and server respectively.

PacketEvents does not provide wrappers for every packet type, and the provided wrappers for some packet types do not
work with every protocol version. The preferred way to handle these packets is to define our own wrapper in the
`wrappers` subpackage, and potentially pull request it to PacketEvents if it would be of widespread utility.

Step 0: Wait
------------
Wait for the dependencies to be updated. That typically means PacketEvents. It must have at least released a development
build that works with the new Minecraft version.

When dependencies are fully updated, bump any dependency versions in `gradle/libs.versions.toml`. You
should not need to bump the API version or Paper version in `gradle.properties`, and if you do, be sure to maintain 
backwards compatibility with previous server versions.

Step 1: Fix Existing Offsetters
-------------------------------
Starting at the top of the list in [Testing.md](./Testing.md), execute each existing test in the new version. If a test
case does not work or results in a console error, investigate changes in the protocol related to that feature. Some
resources which might be helpful:
* [Minecraft Wiki protocol documentation](https://minecraft.wiki/w/Java_Edition_protocol)
* [PacketEvents Git history](https://github.com/retrooper/packetevents/commits/2.0/)
* [Prismarine minecraft-data protocol docs](https://prismarinejs.github.io/minecraft-data/protocol/)

The easiest way to debug wrappers from PacketEvents is to use a debugger in IntelliJ or similar. There are plenty of
guides online for how to attach a debugger to the running Minecraft server and place breakpoints within plugin code.

PacketEvents should provide backwards compatibility for most changes, but if needed, use version checks. 

Step 2: Add New Packets
-----------------------
New protocol versions likely added some new packet types. It is critical that every packet containing an absolute
coordinate is registered in an Offsetter so that the plugin can obfuscate that coordinate. The easiest way to do this
is to look at the Minecraft changelog, and try out all of the new features in that version to make sure that none of
them are broken. Do this while confirming that your coordinates displayed in F3 are **not** the world's real
coordinates.

Step 3: Testing
---------------
Now it's time to test everything. I summarized everything that tends to exhibit problems in [Testing.md](Testing.md).
It's best to run through the list on some older versions if possible.

Step 4: Done!
-------------
