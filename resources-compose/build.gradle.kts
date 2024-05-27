import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("android-base-convention")
    id("dev.icerock.mobile.multiplatform.android-manifest")
    id("multiplatform-android-publish-convention")
    id("apple-main-convention")
    id("detekt-convention")
    id("org.jetbrains.compose")
    id("javadoc-stub-convention")
    id("publication-convention")
}

android {
    defaultConfig {
        minSdk = 21
    }

    namespace = "dev.icerock.moko.resources.compose"
}

kotlin {
    jvm()
    androidTarget()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    macosX64()
    macosArm64()
    js(IR) {
        browser()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain {
            dependencies {
                api(projects.resources)
                api(compose.runtime)
                api(compose.foundation)
            }
        }
        androidMain {
            dependencies {
                api(libs.composeUi)
            }
        }
        val commonJsMain by creating {
            dependsOn(commonMain.get())
        }
        val wasmJsMain by getting {
            dependsOn(commonJsMain)
        }
        jsMain {
            dependsOn(commonJsMain)
        }
    }
}
