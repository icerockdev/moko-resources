/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("multiplatform-library-convention")
    id("multiplatform-android-publish-convention")
    id("apple-main-convention")
    id("detekt-convention")
    id("javadoc-stub-convention")
    id("publication-convention")
}

kotlin {
    watchosX64()
    watchosArm64()
    watchosSimulatorArm64()
}

android {
    namespace = "dev.icerock.moko.resources.test"
}

dependencies {
    commonMainApi(projects.resources)
}
