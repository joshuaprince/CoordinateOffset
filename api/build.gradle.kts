plugins {
    alias(libs.plugins.maven.publish)
    idea
    `java-library`
}

dependencies {
    api(libs.configlib.core)

    compileOnly(libs.jspecify)
    compileOnly(libs.paper.api)

    testImplementation(libs.paper.api)
    testImplementation(libs.test.junit.jupiter)
    testRuntimeOnly(libs.test.junit.platform)
}

tasks {
    test {
        useJUnitPlatform()
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates(project.group.toString(), "coordinateoffset-api", project.version.toString())
    pom {
        name = "CoordinateOffset API"
        description = "API for CoordinateOffset, a Minecraft server plugin that configurably obfuscates players' coordinates."
        url = "https://github.com/joshuaprince/CoordinateOffset"
        developers {
            developer {
                id = "joshuaprince"
                name = "Joshua Prince"
                email = "joshua@jtprince.com"
                url = "https://github.com/joshuaprince"
            }
        }
        licenses {
            license {
                name = "GNU Affero General Public License v3.0"
                url = "https://www.gnu.org/licenses/agpl-3.0.en.html"
            }
        }
        scm {
            connection = "scm:git:git://github.com/joshuaprince/CoordinateOffset.git"
            developerConnection = "scm:git:ssh://github.com:joshuaprince/CoordinateOffset.git"
            url = "https://github.com/joshuaprince/CoordinateOffset"
        }
    }
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}
