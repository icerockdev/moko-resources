/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("dev.icerock.mobile.multiplatform")
    id("dev.icerock.mobile.multiplatform-resources")
}

android {
    compileSdkVersion(Versions.Android.compileSdk)

    defaultConfig {
        minSdkVersion(Versions.Android.minSdk)
        targetSdkVersion(Versions.Android.targetSdk)
    }

    lintOptions {
        disable("ImpliedQuantity")
    }
}

setupFramework(exports = emptyList())

dependencies {
    mppLibrary(Deps.Libs.MultiPlatform.kotlinStdLib)
    mppLibrary(Deps.Libs.MultiPlatform.mokoResources)

    commonMainImplementation(project("$path:nested-module"))
}

multiplatformResources {
    multiplatformResourcesPackage = "com.icerockdev.library"
}

// uncomment to test static framework
// also in sample/mpp-library/MultiPlatformLibrary.podspec:14 flag should be uncommented
//kotlin {
//    targets
//        .filterIsInstance<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>()
//        .flatMap { it.binaries }
//        .filterIsInstance<org.jetbrains.kotlin.gradle.plugin.mpp.Framework>()
//        .forEach { framework ->
//            framework.isStatic = true
//        }
//}
