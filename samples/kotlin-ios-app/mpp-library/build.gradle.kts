/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("dev.icerock.mobile.multiplatform-resources")
}

kotlin {
    iosArm64()
    iosSimulatorArm64()
    iosX64()
    val watchosX64 = watchosX64()
    val watchosArm64 = watchosArm64()
    val watchosSimulatorArm64 = watchosSimulatorArm64()
    configure(listOf(watchosX64, watchosArm64, watchosSimulatorArm64)) {
        binaries {
            framework {
                baseName = "MppLibrary"
            }
        }
    }


    sourceSets {
        val commonMain by getting

        val appleMain by creating {
            dependsOn(commonMain)
            dependencies {
                api(moko.resources)
            }
        }

        val iosMain by creating {
            dependsOn(appleMain)
        }
        val iosArm64Main by getting {
            dependsOn(iosMain)
        }
        val iosX64Main by getting {
            dependsOn(iosMain)
        }
        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }

        val watchosMain by creating {
            dependsOn(appleMain)
        }
        val watchosX64Main by getting
        val watchosArm64Main by getting
        val watchosSimulatorArm64Main by getting
        watchosX64Main.dependsOn(watchosMain)
        watchosArm64Main.dependsOn(watchosMain)
        watchosSimulatorArm64Main.dependsOn(watchosMain)
    }
}

multiplatformResources {
    resourcesPackage.set("com.icerockdev.library")
}
