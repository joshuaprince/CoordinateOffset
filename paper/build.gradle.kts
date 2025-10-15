plugins {
    java
    id("com.gradleup.shadow") version "9.2.2"
}

project.group = "com.jtprince.coordinateoffset"

dependencies {
    shadow(libs.paper.api)
    shadow(libs.packetevents.api)

    implementation(project(":api"))
    implementation(project(":core"))
    implementation(libs.bstats.bukkit)
    implementation(libs.configlib.yaml)
    implementation(libs.morepdt)

    testImplementation(libs.paper.api)
    testImplementation(libs.test.junit.jupiter)
    testRuntimeOnly(libs.test.junit.platform)
}

tasks {
    processResources {
        inputs.property("apiVersion", libs.versions.paper.apiversion)
        val placeholders = mapOf(
            "version" to version.toString().trimStart('v'),
            "apiVersion" to libs.versions.paper.apiversion.get(),
        )
        placeholders.forEach { (k, v) -> inputs.property(k, v) } // ensure cache is invalidated after version bumps
        files(listOf("paper-plugin.yml", "config.yml")) {
            expand(placeholders)
        }
    }

    jar {
        archiveClassifier.set("thin")
    }

    shadowJar {
        archiveFileName.set("${rootProject.name}-Paper.jar")
        relocate("org.bstats", "${project.group}.lib.org.bstats")
        relocate("com.jeff_media", "${project.group}.lib.com.jeff_media")
        relocate("de.exlll.configlib", "${project.group}.lib.de.exlll.configlib")
        relocate("org.snakeyaml", "${project.group}.lib.org.snakeyaml")
        minimize()
    }

    register<Copy>("buildSnapshot") {
        // Copy the latest artifact from `assemble` task to a consistent place for symlinking into a server.
        dependsOn(shadowJar)
        from(shadowJar)
        into("build")
        val projectName = rootProject.name
        rename { "$projectName-Paper-SNAPSHOT.jar" }
    }

    assemble {
        dependsOn(shadowJar)
    }

    test {
        useJUnitPlatform()
    }
}
