/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("com.android.library")
    id("android-base-convention")
    id("org.jetbrains.kotlin.multiplatform")
    id("detekt-convention")
    id("org.jetbrains.compose")
    id("javadoc-stub-convention")
    id("multiplatform-android-publish-convention")
    id("publication-convention")
    id("dev.icerock.mobile.multiplatform.android-manifest")
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
        commonMain {
            dependencies {
                api(projects.resources)
                implementation(compose.runtime)
                implementation(compose.foundation)
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
