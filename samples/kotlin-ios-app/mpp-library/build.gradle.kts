/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("dev.icerock.mobile.multiplatform-resources")
}

kotlin {
    applyDefaultHierarchyTemplate()

    iosArm64()
    iosX64()
    iosSimulatorArm64()

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

        val appleMain by getting {
            dependencies {
                api(moko.resources)
            }
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

multiplatformResources {
    resourcesPackage.set("com.icerockdev.library")
}
