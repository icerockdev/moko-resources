/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("com.android.library")
    id("android-base-convention")
    id("org.jetbrains.kotlin.multiplatform")
    id("dev.icerock.mobile.multiplatform-resources")
    id("detekt-convention")
}

android {
    lintOptions {
        disable("ImpliedQuantity")
    }
}

kotlin {
    android()
    iosX64 {
        binaries.framework {
            baseName = "MultiPlatformLibrary"
            isStatic = false
        }
    }
    iosArm64 {
        binaries.framework {
            baseName = "MultiPlatformLibrary"
            isStatic = false
        }
    }
    iosSimulatorArm64 {
        binaries.framework {
            baseName = "MultiPlatformLibrary"
            isStatic = false
        }
    }

    sourceSets {
        val iosX64Main by getting {}
        val iosX64Test by getting {}
        val iosMiddle by creating {
            dependsOn(iosX64Main)
        }
        val iosArm64Main by getting {
            dependsOn(iosMiddle)
        }
        val iosArm64Test by getting {
            dependsOn(iosX64Test)
        }
        val iosSimulatorArm64Main by getting {
            dependsOn(iosMiddle)
        }
        val iosSimulatorArm64Test by getting {
            dependsOn(iosX64Test)
        }
    }
}

dependencies {
    commonMainApi(projects.resources)
}

multiplatformResources {
    multiplatformResourcesPackage = "com.icerockdev.library"
}

// skip task because it's failed on gradle 7 and we not use results of this processing
tasks.getByName("iosX64ProcessResources").enabled = false
