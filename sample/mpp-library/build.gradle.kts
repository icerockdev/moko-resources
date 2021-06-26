/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("com.android.library")
    id("android-base-convention")
    id("org.jetbrains.kotlin.multiplatform")
    id("dev.icerock.mobile.multiplatform.android-manifest")
    id("dev.icerock.mobile.multiplatform.apple-framework")
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
    ios()
    macosX64()
}

framework {
    export(projects.resources)
}

dependencies {
    commonMainApi(projects.resources)
    commonMainImplementation(projects.sample.mppLibrary.nestedModule)

    commonTestImplementation(libs.kotlinTest)
    commonTestImplementation(libs.kotlinTestAnnotations)
    commonTestImplementation(projects.resourcesTest)

    androidTestImplementation(libs.kotlinTestJUnit)
}

multiplatformResources {
    multiplatformResourcesPackage = "com.icerockdev.library"
}
