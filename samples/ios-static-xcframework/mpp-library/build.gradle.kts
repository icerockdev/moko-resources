/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("dev.icerock.mobile.multiplatform-resources")
}

kotlin {
    val iosArm64 = iosArm64()
    val iosX64 = iosX64()
    val iosSimulatorArm64 = iosSimulatorArm64()

    val xcFramework = XCFramework("MultiPlatformLibrary")
    configure(listOf(iosArm64, iosX64, iosSimulatorArm64)) {
        binaries {
            framework {
                isStatic = true
                baseName = "MultiPlatformLibrary"
                xcFramework.add(this)
                export(moko.resources)
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(moko.resources)
            }
        }
        val iosMain by creating {
            dependsOn(commonMain)
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
    }
}

dependencies {
    commonMainApi(moko.resources)
}

multiplatformResources {
    multiplatformResourcesPackage = "com.icerockdev.library"
}
