/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    plugin(Deps.Plugins.androidLibrary)
    plugin(Deps.Plugins.kotlinMultiplatform)
    plugin(Deps.Plugins.mobileMultiplatform)
    plugin(Deps.Plugins.iosFramework)
    plugin(Deps.Plugins.mokoResources)
}

android {
    lintOptions {
        disable("ImpliedQuantity")
    }
}

dependencies {
    commonMainImplementation(Deps.Libs.MultiPlatform.mokoResources)

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
