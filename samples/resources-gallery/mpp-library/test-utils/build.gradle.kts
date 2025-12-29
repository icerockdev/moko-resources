/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
}

android {
    namespace = "com.icerockdev.library.testutils"
}

// disable android lint for test utils (no need here)
tasks.matching { it.name.startsWith("lint") }.configureEach { enabled = false }

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api("org.jetbrains.kotlin:kotlin-test:2.1.0")
                api("org.jetbrains.kotlin:kotlin-test-annotations-common:2.1.0")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
                api(moko.resourcesTest)
            }
        }

        val jvmMain by getting {
            dependencies {
                api("org.jetbrains.kotlin:kotlin-test-junit:2.1.0")
            }
        }

        val androidMain by getting {
            dependencies {
                api("org.jetbrains.kotlin:kotlin-test-junit:2.1.0")
                api("androidx.test:core:1.6.1")
                api("org.robolectric:robolectric:4.12.2")
                api("junit:junit:4.13.2")
            }
        }
    }
}
