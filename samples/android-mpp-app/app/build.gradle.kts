/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.multiplatform")
    id("dev.icerock.mobile.multiplatform-resources")
}

android {
    namespace = "com.icerockdev.mpp"

    compileSdk = 36

    defaultConfig {
        minSdk = 16
        targetSdk = 36
    }

    defaultConfig {
        applicationId = "com.icerockdev.mpp"

        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    androidTarget()
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")

    commonMainImplementation(moko.resources)
}

multiplatformResources {
    resourcesPackage.set("com.icerockdev.library")
}
