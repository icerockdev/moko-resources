/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("multiplatform-library-convention")
}

kotlin {
    sourceSets {
        val commonMain by getting

        val appleMain by creating {
            dependsOn(commonMain)
        }
        val iosMain by getting {
            dependsOn(appleMain)
        }
        val macosMain by getting {
            dependsOn(appleMain)
        }

        val commonTest by getting
        val appleTest by creating {
            dependsOn(commonTest)
        }
        val iosTest by getting {
            dependsOn(appleTest)
        }
        val macosTest by getting {
            dependsOn(appleTest)
        }
    }

    sourceSets.matching {
        it.name == "watchosMain"
    }.configureEach {
        this.dependsOn(sourceSets.getByName("appleMain"))
    }

    sourceSets.matching {
        it.name == "tvosMain"
    }.configureEach {
        this.dependsOn(sourceSets.getByName("appleMain"))
    }
}
