/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("dev.icerock.mobile.multiplatform-resources")
}

kotlin {
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    listOf(watchosX64(), watchosArm64(), watchosSimulatorArm64()).forEach { target ->
        target.binaries {
            framework {
                baseName = "MppLibrary"
            }
        }
    }

    applyDefaultHierarchyTemplate()
    sourceSets {
        commonMain.dependencies {
            api(moko.resources)
        }
    }
}

multiplatformResources {
    resourcesPackage.set("com.icerockdev.library")
}
