/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("multiplatform-library-convention")
    id("detekt-convention")
}

// disable android lint for test utils (no need here)
tasks.matching { it.name.startsWith("lint") }.configureEach { enabled = false }

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.kotlinTest)
                api(libs.kotlinTestAnnotations)
                api(projects.resources)
            }
        }

        val jvmMain by getting {
            dependencies {
                api(libs.kotlinTestJUnit)
            }
        }

        val androidMain by getting {
            dependencies {
                api(libs.kotlinTestJUnit)
                api(libs.testCore)
                api(libs.robolectric)
            }
        }
    }
}
