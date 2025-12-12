dependencies {
    compileOnly(libs.adventure.api)
    compileOnly(libs.adventure.minimessage)
    compileOnly(libs.geyser.api)
    compileOnly(libs.jspecify)
    compileOnly(libs.packetevents.api)

    implementation(project(":api"))
    implementation(libs.configlib.paper) // TODO: Minor leak of Paper platform into core
    implementation(libs.netty.buffer)

    testCompileOnly(libs.jspecify)
    testImplementation(libs.test.junit.jupiter)
    testRuntimeOnly(libs.test.junit.platform)
}

tasks.test {
    useJUnitPlatform()
}
