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
    jvm()
}

dependencies {
    commonMainApi(Deps.Libs.MultiPlatform.mokoResources)
    commonMainApi(Deps.Libs.MultiPlatform.mokoGraphics.common)
    commonMainImplementation(project("$path:nested-module"))

    androidTestImplementation(Deps.Libs.Android.Tests.kotlinTestJUnit)
    androidTestImplementation(Deps.Libs.Android.Tests.testCore)

    commonTestImplementation(Deps.Libs.MultiPlatform.Tests.kotlinTest)
    commonTestImplementation(Deps.Libs.MultiPlatform.Tests.kotlinTestAnnotations)
    commonTestImplementation(Deps.Libs.MultiPlatform.mokoResourcesTest)
}

multiplatformResources {
    multiplatformResourcesPackage = "com.icerockdev.library"
}

framework {
    export(Deps.Libs.MultiPlatform.mokoGraphics)
    export(project(":resources"))
}
