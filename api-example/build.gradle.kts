plugins {
    java
}

group = "com.jtprince"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    mavenLocal()
    // maven("https://jitpack.io") // For CoordinateOffset
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly("com.jtprince:CoordinateOffset:master-SNAPSHOT")
}

java {
    val javaVersion: JavaLanguageVersion = JavaLanguageVersion.of(17)
    toolchain {
        sourceCompatibility = JavaVersion.toVersion(javaVersion)
        targetCompatibility = JavaVersion.toVersion(javaVersion)
        languageVersion.set(javaVersion)
    }
}
