rootProject.name = "kotlin-2-tests"
include(":shared")

pluginManagement {
    repositories {
        mavenLocal()
        google()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
    }

    versionCatalogs {
        create("moko") {
            from(files("../../gradle/moko.versions.toml"))
        }
    }
}
