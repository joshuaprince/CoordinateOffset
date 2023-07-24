plugins {
    java
}

group = "com.jtprince"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://jitpack.io") // For CoordinateOffset
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:24.0.0")
    compileOnly("com.github.dmulloy2:ProtocolLib:master-SNAPSHOT")
    compileOnly("com.jtprince:CoordinateOffset:v2.1.0")
}

java {
    val javaVersion: JavaLanguageVersion = JavaLanguageVersion.of(17)
    toolchain {
        sourceCompatibility = JavaVersion.toVersion(javaVersion)
        targetCompatibility = JavaVersion.toVersion(javaVersion)
        languageVersion.set(javaVersion)
    }
}
