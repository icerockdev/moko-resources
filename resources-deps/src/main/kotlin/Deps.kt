/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

object Deps {
    private const val kotlinVersion = "1.4.31"
    private const val androidGradleVersion = "4.1.2"

    private const val androidAppCompatVersion = "1.1.0"
    private const val espressoCoreVersion = "3.2.0"
    private const val testRunnerVersion = "1.2.0"
    private const val testExtJunitVersion = "1.1.1"

    private const val apacheCommonsTextVersion = "1.3"
    private const val kotlinPoetVersion = "1.6.0"
    private const val kotlinxSerializationVersion = "1.0.0"

    private const val detektVersion = "1.15.0"

    private const val mokoGraphicsVersion = "0.5.0"
    private const val mokoParcelizeVersion = "0.5.0"
    const val mokoResourcesVersion = "0.15.1"

    object Android {
        const val compileSdk = 28
        const val targetSdk = 28
        const val minSdk = 16
    }

    object Plugins {
        val javaGradle = GradlePlugin(id = "java-gradle-plugin")
        val androidApplication = GradlePlugin(id = "com.android.application")
        val androidLibrary = GradlePlugin(id = "com.android.library")
        val kotlinJvm = GradlePlugin(id = "org.jetbrains.kotlin.jvm")
        val kotlinMultiplatform = GradlePlugin(id = "org.jetbrains.kotlin.multiplatform")
        val kotlinKapt = GradlePlugin(id = "kotlin-kapt")
        val kotlinAndroid = GradlePlugin(id = "kotlin-android")
        val kotlinParcelize = GradlePlugin(id = "kotlin-parcelize")
        val kotlinSerialization = GradlePlugin(id = "kotlin-serialization")
        val mavenPublish = GradlePlugin(id = "org.gradle.maven-publish")

        val mobileMultiplatform = GradlePlugin(id = "dev.icerock.mobile.multiplatform")
        val appleFramework = GradlePlugin(id = "dev.icerock.mobile.multiplatform.apple-framework")

        val mokoResources = GradlePlugin(
            id = "dev.icerock.mobile.multiplatform-resources",
            module = "dev.icerock.moko:resources-generator:$mokoResourcesVersion"
        )
    }

    object Libs {
        object Android {
            const val appCompat =
                "androidx.appcompat:appcompat:$androidAppCompatVersion"

            object Tests {
                const val espressoCore =
                    "androidx.test.espresso:espresso-core:$espressoCoreVersion"
                const val kotlinTestJUnit =
                    "org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion"
                const val testCore =
                    "androidx.test:core:1.3.0"
                const val robolectric =
                    "org.robolectric:robolectric:4.4"
                const val testRunner =
                    "androidx.test:runner:$testRunnerVersion"
                const val testRules =
                    "androidx.test:rules:$testRunnerVersion"
                const val testExtJunit =
                    "androidx.test.ext:junit:$testExtJunitVersion"
            }
        }

        object MultiPlatform {
            const val mokoResources = "dev.icerock.moko:resources:$mokoResourcesVersion"
            const val mokoParcelize = "dev.icerock.moko:parcelize:$mokoParcelizeVersion"
            val mokoGraphics = "dev.icerock.moko:graphics:$mokoGraphicsVersion"
                .defaultMPL(android = true, ios = true, macos = true)

            object Tests {
                const val kotlinTest =
                    "org.jetbrains.kotlin:kotlin-test-common:$kotlinVersion"
                const val kotlinTestAnnotations =
                    "org.jetbrains.kotlin:kotlin-test-annotations-common:$kotlinVersion"
            }
        }

        object Jvm {
            const val kotlinPoet =
                "com.squareup:kotlinpoet:$kotlinPoetVersion"
            const val kotlinxSerialization =
                "org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion"
            const val apacheCommonsText =
                "org.apache.commons:commons-text:$apacheCommonsTextVersion"
            const val detektFormatting =
                "io.gitlab.arturbosch.detekt:detekt-formatting:$detektVersion"
            const val kotlinGradlePlugin =
                "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"
            const val androidGradlePlugin =
                "com.android.tools.build:gradle:$androidGradleVersion"
            const val kotlinCompilerEmbeddable =
                "org.jetbrains.kotlin:kotlin-compiler-embeddable:$kotlinVersion"
        }
    }
}
