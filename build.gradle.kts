plugins {
    java
    id("com.gradleup.shadow") version "9.2.2" apply false
    id("com.vanniktech.maven.publish") version "0.34.0" apply false
}

allprojects {
    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.codemc.org/repository/maven-snapshots/") // PacketEvents
    }

    group = "com.jtprince.coordinateoffset"
    version = "5.0.0"
}

subprojects {
    apply(plugin = "java")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }
}
