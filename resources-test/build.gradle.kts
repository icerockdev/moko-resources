/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("android-base-convention")
    id("dev.icerock.mobile.multiplatform.android-manifest")
    id("detekt-convention")
    id("javadoc-stub-convention")
    id("publication-convention")
}

group = "dev.icerock.moko"
version = libs.versions.mokoResourcesVersion.get()

kotlin {
    android {
        publishLibraryVariants("release", "debug")
    }
    ios()
    macosX64()
    sourceSets {
        val commonMain by getting {}

        val appleMain by creating {
            dependsOn(commonMain)
        }
        val iosMain by getting {
            dependsOn(appleMain)
        }
        val macosX64Main by getting {
            dependsOn(appleMain)
        }
    }
}

dependencies {
    commonMainApi(project(":resources"))
}
