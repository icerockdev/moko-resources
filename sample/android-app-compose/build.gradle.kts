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
    implementation(libs.appCompat)
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
        minSdkVersion(21)
    }

    composeOptions {
        kotlinCompilerVersion = "1.5.10"
        kotlinCompilerExtensionVersion = "1.0.0-rc02"
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + listOf(
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:suppressKotlinVersionCompatibilityCheck=true"
        )
    }
}