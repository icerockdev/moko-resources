pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }

    versionCatalogs {
        create("moko") {
            from(files("../../gradle/moko.versions.toml"))
        }
    }
}

rootProject.name = "CM-Resources-Sample"
include(":composeApp")
