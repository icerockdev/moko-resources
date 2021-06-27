/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("android-app-convention")
    id("org.jetbrains.kotlin.multiplatform")
    id("dev.icerock.mobile.multiplatform-resources")
    id("detekt-convention")
}

android {
    buildFeatures.dataBinding = true

    defaultConfig {
        applicationId = "dev.icerock.moko.samples.resources.mpp"

        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

kotlin {
    android()
    ios()
}

dependencies {
    "androidMainImplementation"(libs.appCompat)

    commonMainImplementation(projects.resources)
}

multiplatformResources {
    multiplatformResourcesPackage = "com.icerockdev.library"
    disableStaticFrameworkWarning = true
}
