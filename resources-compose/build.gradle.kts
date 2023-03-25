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
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

kotlin {
    jvm()
    android()
    ios()
    iosSimulatorArm64()
    macosX64()
    macosArm64()
    js(IR) {
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

        named("androidMain") {
            dependencies {
                api(libs.composeUi)
            }
        }

        named("jvmMain") {
            dependencies {
                api(compose.desktop.common)
                implementation(compose.desktop.currentOs)
            }
        }
    }
}
