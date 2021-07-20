/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */
plugins {
    id("android-app-convention")
    id("dev.icerock.mobile.multiplatform-resources")
    id("kotlin-android")
}

dependencies {
    implementation(projects.sample.mppLibrary)
    implementation(libs.mokoResources)
    implementation(libs.appCompatAlpha)
    implementation(libs.composeUi)
    implementation(libs.composeUiTooling)
    implementation(libs.composeUiToolingPreview)
    implementation(libs.composeRuntime)
    implementation(libs.composeMaterial)
    implementation(libs.composeFoundation)
    implementation(libs.composeActivity)
    implementation(libs.constraintLayout)
}

android {

    defaultConfig {
        applicationId = "dev.icerock.moko.samples.compose"
    }

    buildFeatures {
        compose = true
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
        freeCompilerArgs = freeCompilerArgs +
                "-Xopt-in=kotlin.RequiresOptIn" +
                "-Xexplicit-api=strict"
    }

    composeOptions {
        kotlinCompilerVersion = "1.5.20"
        kotlinCompilerExtensionVersion = "1.0.0-rc02"
    }
}