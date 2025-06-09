/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
        mavenLocal()
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

include(":mpp-library")
include(":mpp-library:test-utils")
include(":android-app")
