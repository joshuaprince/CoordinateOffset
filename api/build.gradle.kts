plugins {
    java
    `java-library`
}

dependencies {
    compileOnly(libs.paper.api)
    api(libs.packetevents.api)
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
