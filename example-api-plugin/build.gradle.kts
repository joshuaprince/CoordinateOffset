plugins {
    id("java")
    id("com.gradleup.shadow") version "9.2.2"
}

project.group = "com.jtprince.coordinateoffset.example"

dependencies {
    /*
     * Use the following replacements in your own plugin:
     *  shadow("io.papermc.paper:paper-api:<VERSION>-R0.1-SNAPSHOT")
     *  shadow("com.jtprince.coordinateoffset:coordinateoffset-api:<VERSION>")
     * CoordinateOffset API version examples:
     *  - 5.0
     *  - 5.1-SNAPSHOT
     */
    shadow(libs.paper.api)
    shadow(project(":api"))
}

tasks {
    shadowJar {
        archiveFileName.set("CoordinateOffsetAPIExample.jar")
        minimize()
    }
    assemble {
        dependsOn(shadowJar)
    }
}
