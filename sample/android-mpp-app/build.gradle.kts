/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.multiplatform")
    id("dev.icerock.mobile.multiplatform-resources")
}

android {
    compileSdkVersion(libs.versions.compileSdk.get().toInt())

    buildFeatures.dataBinding = true

    dexOptions {
        javaMaxHeapSize = "2g"
    }

    defaultConfig {
        minSdkVersion(libs.versions.minSdk.get().toInt())
        targetSdkVersion(libs.versions.targetSdk.get().toInt())

        applicationId = "dev.icerock.moko.samples.resources.mpp"

        versionCode = 1
        versionName = "0.1.0"

        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
        getByName("debug") {
            isDebuggable = true
            applicationIdSuffix = ".debug"
        }
    }

    packagingOptions {
        exclude("META-INF/*.kotlin_module")
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
