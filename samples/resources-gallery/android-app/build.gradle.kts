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
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation(project(":mpp-library"))
}

afterEvaluate {
    tasks.withType<JavaCompile>().configureEach {
        sourceCompatibility = JavaVersion.VERSION_17.toString()
        targetCompatibility = JavaVersion.VERSION_17.toString()
    }
}
