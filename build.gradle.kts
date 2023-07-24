buildscript {
    repositories {
        mavenCentral()
    }
}

plugins {
    java
    `java-library`
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("com.palantir.git-version") version "3.0.0"
    `maven-publish`
}

val gitVersion: groovy.lang.Closure<String> by extra

group = "com.jtprince"
version = gitVersion()

val pluginYmlApiVersion: String by project
val spigotApiVersion: String by project
val javaVersion: JavaLanguageVersion = JavaLanguageVersion.of(17)

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://repo.codemc.org/repository/maven-public/") // CommandAPI
    maven("https://hub.jeff-media.com/nexus/repository/jeff-media-public/") // MorePDTs
    maven("https://jitpack.io")  // ProtocolLib (needs to be a dev build)
}

dependencies {
    shadow("org.jetbrains:annotations:24.0.0")
    shadow("org.spigotmc:spigot-api:$spigotApiVersion")
    shadow("com.github.dmulloy2:ProtocolLib:master-SNAPSHOT")
    implementation("dev.jorel:commandapi-bukkit-shade:9.0.3")
    implementation("com.jeff_media:MorePersistentDataTypes:2.4.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.spigotmc:spigot-api:$spigotApiVersion")
    testImplementation("com.github.dmulloy2:ProtocolLib:master-SNAPSHOT")
}

java {
    toolchain {
        sourceCompatibility = JavaVersion.toVersion(javaVersion)
        targetCompatibility = JavaVersion.toVersion(javaVersion)
        languageVersion.set(javaVersion)
    }
}

tasks {
    processResources {
        val placeholders = mapOf(
            "version" to version,
            "apiVersion" to pluginYmlApiVersion,
        )
        placeholders.forEach { (k, v) -> inputs.property(k, v) } // ensure cache is invalidated after version bumps
        filesMatching(listOf("plugin.yml", "config.yml")) {
            expand(placeholders)
        }
    }

    jar {
        archiveClassifier.set("thin")
    }

    shadowJar {
        archiveClassifier.set("")
        minimize()
        relocate("dev.jorel.commandapi", "${project.group}.lib.commandapi")
        relocate("com.jeff_media.morepersistentdatatypes", "${project.group}.lib.morepersistentdatatypes")
    }

    register<Copy>("buildSnapshot") {
        // Copy the latest artifact from `assemble` task to a consistent place for symlinking into a server.
        dependsOn(shadowJar)
        from(shadowJar)
        into("build")
        rename { "${rootProject.name}-SNAPSHOT.jar" }
    }

    assemble {
        dependsOn(shadowJar)
    }

    test {
        useJUnitPlatform()
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            groupId = "com.jtprince"
            artifactId = "CoordinateOffset"
        }
    }
}
