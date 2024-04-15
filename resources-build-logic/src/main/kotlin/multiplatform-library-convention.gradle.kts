/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("android-base-convention")
    id("dev.icerock.mobile.multiplatform.android-manifest")
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
        useCommonJs()
    }

    sourceSets {
        val iosMain by creating
        val iosSimulatorArm64Main by getting
        iosSimulatorArm64Main.dependsOn(iosMain)

        val iosTest by creating
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
        val jsTest by getting
        macosArm64Test.dependsOn(macosTest)
        macosX64Test.dependsOn(macosTest)
        macosTest.dependsOn(commonTest)
        jsTest.dependsOn(commonTest)
    }

    jvmToolchain(11)
}

tasks.withType<AbstractTestTask> {
    testLogging {
        exceptionFormat = TestExceptionFormat.FULL
        events = setOf(
            TestLogEvent.SKIPPED,
            TestLogEvent.PASSED,
            TestLogEvent.FAILED
        )
        showStandardStreams = true
    }
    outputs.upToDateWhen { false }
}
