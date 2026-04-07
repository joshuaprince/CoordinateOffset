plugins {
    java
}

allprojects {
    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.codemc.org/repository/maven-snapshots/") // PacketEvents
        maven("https://repo.opencollab.dev/main/") // Geyser
    }

    group = "com.jtprince.coordinateoffset"
    version = "6.1.4"
}

subprojects {
    apply(plugin = "java")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }
}
