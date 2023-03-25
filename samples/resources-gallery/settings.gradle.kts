/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

enableFeaturePreview("VERSION_CATALOGS")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
        mavenLocal()
    }
    versionCatalogs {
        create("moko") {
            from(files("../../gradle/moko.versions.toml"))
        }
    }
}

include(":mpp-library")
include(":mpp-library:nested-module")
include(":mpp-library:empty-module")
include(":mpp-library:test-utils")
include(":android-app")
include(":jvm-app")
include(":web-app")
