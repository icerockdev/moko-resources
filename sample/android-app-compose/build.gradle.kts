/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("android-app-convention")
    id("dev.icerock.mobile.multiplatform-resources")
    id("kotlin-android")
}

android {

    defaultConfig {
        applicationId = "dev.icerock.moko.samples.compose"
        minSdk = 21
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(projects.resourcesCompose)
    implementation(projects.sample.mppLibrary)
    implementation(libs.appCompat)
}
