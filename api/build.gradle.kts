plugins {
    java
    `java-library`
    id("com.gradleup.shadow") version "9.2.2"
}

project.group = "com.jtprince.coordinateoffset"

dependencies {
    compileOnly(libs.jspecify)

    shadow(libs.configlib.core)
    shadow(libs.packetevents.api)
    shadow(libs.paper.api)
    implementation(libs.morepdt)

    testImplementation(libs.paper.api)
    testImplementation(libs.test.junit.jupiter)
    testRuntimeOnly(libs.test.junit.platform)
}

tasks {
    test {
        useJUnitPlatform()
    }
}
