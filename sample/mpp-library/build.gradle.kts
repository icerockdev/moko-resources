/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("dev.icerock.mobile.multiplatform")
    id("dev.icerock.mobile.multiplatform.apple-framework")
    id("dev.icerock.mobile.multiplatform-resources")
}

android {
    lintOptions {
        disable("ImpliedQuantity")
    }
}

kotlin {
    macosX64()
    targets.withType(org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget::class.java).all {
        binaries.withType(org.jetbrains.kotlin.gradle.plugin.mpp.Framework::class.java).all {
            export(projects.resources)
        }
    }
}

dependencies {
    commonMainApi(libs.mokoResources)
    commonMainApi(libs.mokoGraphics)
    commonMainImplementation(project("$path:nested-module"))
}

multiplatformResources {
    multiplatformResourcesPackage = "com.icerockdev.library"
}

