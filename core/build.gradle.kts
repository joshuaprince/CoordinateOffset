dependencies {
    compileOnly(libs.jspecify)
    compileOnly(libs.packetevents.api)

    implementation(project(":api"))
    implementation(libs.configlib.paper) // TODO: Minor leak of Paper platform into core

    testImplementation(libs.test.junit.jupiter)
    testRuntimeOnly(libs.test.junit.platform)
}

tasks.test {
    useJUnitPlatform()
}
