/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    defaultConfig {
        targetSdk = 35
        applicationId = "dev.icerock.moko.samples.resources"
        versionCode = 1
        versionName = "0.1.0"
    }
    namespace = "com.icerockdev"
}

dependencies {
    implementation(libs.appCompat)
    implementation(project(":mpp-library"))
}
