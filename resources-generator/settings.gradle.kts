/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

rootProject.name = "resources-generator"

pluginManagement {
    repositories {
        mavenCentral()
        google()

        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }

    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
        create("moko") {
            from(files("../gradle/moko.versions.toml"))
        }
    }
}
// support gradle Composite builds - https://github.com/icerockdev/moko-resources/issues/558
if (gradle.parent == null) {
    includeBuild("../resources-build-logic")
}
