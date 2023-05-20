/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    defaultConfig {
        minSdk = 21
        targetSdk = 33
        applicationId = "dev.icerock.moko.samples.resources"
        versionCode = 1
        versionName = "0.1.0"
    }
    namespace = "com.icerockdev"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation(project(":mpp-library"))
}
