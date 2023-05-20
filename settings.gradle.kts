/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()

        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
    }
    versionCatalogs {
        create("moko") {
            from(files("gradle/moko.versions.toml"))
        }
    }
}

includeBuild("resources-build-logic")
includeBuild("resources-generator")

include(":resources")
include(":resources-compose")
include(":resources-test")
