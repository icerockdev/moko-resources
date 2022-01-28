/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("android-base-convention")
    id("dev.icerock.mobile.multiplatform.android-manifest")
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
        useCommonJs()
    }

    sourceSets {
        val iosMain by getting
        val iosSimulatorArm64Main by getting
        iosSimulatorArm64Main.dependsOn(iosMain)

        val iosTest by getting
        val iosSimulatorArm64Test by getting
        iosSimulatorArm64Test.dependsOn(iosTest)

        val commonMain by getting
        val macosMain by creating
        val macosArm64Main by getting
        val macosX64Main by getting
        macosArm64Main.dependsOn(macosMain)
        macosX64Main.dependsOn(macosMain)
        macosMain.dependsOn(commonMain)

        val commonTest by getting
        val macosTest by creating
        val macosArm64Test by getting
        val macosX64Test by getting
        macosArm64Test.dependsOn(macosTest)
        macosX64Test.dependsOn(macosTest)
        macosTest.dependsOn(commonTest)
    }
}
