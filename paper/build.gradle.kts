plugins {
    java
    id("com.gradleup.shadow") version "9.2.2"
}

dependencies {
    implementation(project(":api"))

    shadow(libs.paper.api)
    shadow(libs.packetevents.api)

    implementation(libs.bstats.bukkit)
    implementation(libs.morepdt)

    testImplementation(libs.paper.api)
    testImplementation(libs.test.junit.jupiter)
    testRuntimeOnly(libs.test.junit.platform)
}

tasks {
    processResources {
        val placeholders = mapOf(
            "version" to version.toString().trimStart('v'),
            "apiVersion" to libs.versions.paper.apiversion,
        )
        placeholders.forEach { (k, v) -> inputs.property(k, v) } // ensure cache is invalidated after version bumps
        files(listOf("plugin.yml", "config.yml")) {
            expand(placeholders)
        }
    }

    test {
        useJUnitPlatform()
    }
}
