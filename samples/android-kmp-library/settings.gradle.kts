pluginManagement {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
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

rootProject.name = "android-kmp-library"
include(":core")
