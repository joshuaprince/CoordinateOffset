plugins {
    `maven-publish`
    signing
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
        register<MavenPublication>("api") {
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
                scm {
                    connection = "scm:git:git://github.com/joshuaprince/CoordinateOffset.git"
                    developerConnection = "scm:git:ssh://github.com:joshuaprince/CoordinateOffset.git"
                    url = "https://github.com/joshuaprince/CoordinateOffset"
                }
            }
        }
    }

    repositories {
        maven {
            name = "CentralRelease"
            url = uri("https://central.sonatype.com/publish/release")
            credentials {
                username = System.getenv("CENTRAL_TOKEN_USERNAME")
                password = System.getenv("CENTRAL_TOKEN_PASSWORD")
            }
        }
        maven {
            name = "CentralSnapshot"
            url = uri("https://central.sonatype.com/repository/maven-snapshots")
            credentials {
                username = System.getenv("CENTRAL_TOKEN_USERNAME")
                password = System.getenv("CENTRAL_TOKEN_PASSWORD")
            }
        }
    }
}

signing {
    useInMemoryPgpKeys(System.getenv("GPG_PRIVATE_KEY"), "")
    sign(publishing.publications["api"])
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}
