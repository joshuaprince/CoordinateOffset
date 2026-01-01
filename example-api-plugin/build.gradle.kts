group = "com.jtprince.coordinateoffset.example"
version = "0.0.1"

dependencies {
    /*
     * Use the following dependencies in your own plugin:
     *  shadow("io.papermc.paper:paper-api:<VERSION>-R0.1-SNAPSHOT")
     *  shadow("com.jtprince.coordinateoffset:coordinateoffset-api:<VERSION>")
     * CoordinateOffset API version examples:
     *  - 6.1.2
     *  - 6.1.3-SNAPSHOT
     */
    compileOnly(libs.paper.api)
    compileOnly(project(":api"))
}

tasks {
    jar {
        archiveBaseName.set("CoordinateOffsetAPIExamplePlugin")
    }
}
