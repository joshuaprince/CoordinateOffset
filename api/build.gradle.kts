plugins {
    `maven-publish`
    idea
}

dependencies {
    compileOnly(libs.jspecify)
    compileOnly(libs.configlib.core)
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

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        register<MavenPublication>("maven") {
            from(components["java"])
            groupId = project.group.toString()
            artifactId = "coordinateoffset-api"
            version = project.version.toString()

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
            }
        }
    }
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}
