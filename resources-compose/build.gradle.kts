/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("com.android.library")
    id("android-base-convention")
    id("org.jetbrains.kotlin.multiplatform")
    id("detekt-convention")
}

android {
    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.0.1"
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

kotlin {
    android()
    jvm()
    sourceSets {
        named("jvmMain") {
            dependencies {
                implementation(projects.sample.mppLibrary)
            }
        }
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
