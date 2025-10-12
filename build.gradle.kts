buildscript {
    repositories {
        mavenCentral()
    }
}

plugins {
    java
    `java-library`
    id("com.gradleup.shadow") version "9.2.2"
    id("com.palantir.git-version") version "4.0.0"
    `maven-publish`
}

val gitVersion: groovy.lang.Closure<String> by extra

group = "com.jtprince"
version = gitVersion()

val pluginYmlApiVersion: String by project
val paperApiVersion: String by project
val javaVersion: JavaLanguageVersion = JavaLanguageVersion.of(17)

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.org/repository/maven-public/") // PacketEvents
}

dependencies {
    // API dependencies: Not shaded into plugin, but needed for API consumers
    api("org.jetbrains:annotations:24.0.1")
    // Compile Only dependencies: Neither shaded nor needed by API consumers (assumed they'll already add it themselves)
    compileOnly("io.papermc.paper:paper-api:$paperApiVersion")
    // TODO: Remove local dependency when PacketEvents for MC 1.21.9+ is available on a public repo
    // TODO: Consider an api dependency
    compileOnly(files("./lib/packetevents-spigot-2.9.6-SNAPSHOT.jar"))

    // Shade and Relocate: Shaded into plugin, exposed to API consumers with relocated names if necessary
    implementation("org.bstats:bstats-bukkit:3.0.2")
    implementation("com.jeff-media:MorePersistentDataTypes:2.4.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    testImplementation("io.papermc.paper:paper-api:$paperApiVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
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
            "version" to version.toString().trimStart('v'),
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
        enableAutoRelocation = true
        relocationPrefix = "${project.group}.coordinateoffset.lib"
        minimize()
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
