plugins {
    java
    id("com.gradleup.shadow") version "9.2.2"
}

project.group = "com.jtprince.coordinateoffset"

dependencies {
    compileOnly(libs.jspecify)
    compileOnly(libs.jetbrains.annotations)

    shadow(libs.packetevents.api)

    implementation(project(":api"))

    testImplementation(libs.test.junit.jupiter)
    testRuntimeOnly(libs.test.junit.platform)
}

tasks.test {
    useJUnitPlatform()
}
