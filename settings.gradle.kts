rootProject.name = "CoordinateOffset"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.codemc.org/repository/maven-snapshots/") // PacketEvents
    }
}

include("api")
include("paper")
