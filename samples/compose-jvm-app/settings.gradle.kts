pluginManagement {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
        mavenLocal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
    }

    versionCatalogs {
        create("libs") {
            from(files("../../gradle/libs.versions.toml"))
        }
        create("moko") {
            from(files("../../gradle/moko.versions.toml"))
        }
    }
}

rootProject.name = "compose-jvm-app"

include(":common")
