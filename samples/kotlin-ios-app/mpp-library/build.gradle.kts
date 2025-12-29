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

    watchosArm64()
    watchosX64()
    watchosSimulatorArm64()

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
