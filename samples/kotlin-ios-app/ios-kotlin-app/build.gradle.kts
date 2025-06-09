/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    kotlin("multiplatform")
    id("dev.icerock.mobile.multiplatform-resources")
}

kotlin {
    listOf(iosArm64(), iosSimulatorArm64(), iosX64()).forEach { target ->
        target.binaries.executable {
            entryPoint = "main"
        }
    }

    applyDefaultHierarchyTemplate()
    sourceSets {
        commonMain.dependencies {
            implementation(project(":mpp-library"))
        }
    }
}

multiplatformResources {
    resourcesPackage.set("com.icerockdev.app")
}
