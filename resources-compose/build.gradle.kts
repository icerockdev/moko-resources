/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("multiplatform-library-convention")
    id("detekt-convention")
    id("publication-convention")
}

android {
    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.0.1"
    }
}

dependencies {
    implementation(libs.composeUiTooling)
    implementation(libs.composeUiToolingPreview)
    implementation(libs.composeRuntime)
    implementation(libs.composeMaterial)
    implementation(libs.composeFoundation)
    implementation(libs.composeActivity)
    implementation(libs.constraintLayout)
}
