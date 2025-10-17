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
