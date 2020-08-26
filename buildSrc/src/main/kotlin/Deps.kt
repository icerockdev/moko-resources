/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

object Deps {
    private const val kotlinVersion = "1.4.0"
    private const val androidGradleVersion = "4.0.1"

    private const val androidAppCompatVersion = "1.1.0"
    private const val espressoCoreVersion = "3.2.0"
    private const val testRunnerVersion = "1.2.0"
    private const val testExtJunitVersion = "1.1.1"

    private const val apacheCommonsTextVersion = "1.3"
    private const val kotlinPoetVersion = "1.6.0"
    private const val kotlinxSerializationVersion = "0.20.0"

    private const val detektVersion = "1.7.4"

    private const val mokoGraphicsVersion = "0.4.0"
    private const val mokoParcelizeVersion = "0.4.0"
    const val mokoResourcesVersion = "0.12.0"

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
        val kotlinAndroidExtensions = GradlePlugin(id = "kotlin-android-extensions")
        val kotlinSerialization = GradlePlugin(id = "kotlin-serialization")
        val mavenPublish = GradlePlugin(id = "org.gradle.maven-publish")

        val mobileMultiplatform = GradlePlugin(id = "dev.icerock.mobile.multiplatform")
        val iosFramework = GradlePlugin(id = "dev.icerock.mobile.multiplatform.ios-framework")

        val mokoResources = GradlePlugin(
            id = "dev.icerock.mobile.multiplatform-resources",
            module = "dev.icerock.moko:resources-generator:$mokoResourcesVersion"
        )

        val detekt = GradlePlugin(
            id = "io.gitlab.arturbosch.detekt",
            version = detektVersion
        )
    }

    object Libs {
        object Android {
            val appCompat =
                AndroidLibrary(name = "androidx.appcompat:appcompat:$androidAppCompatVersion")

            object AndroidTest {
                val espressoCore =
                    AndroidLibrary(name = "androidx.test.espresso:espresso-core:$espressoCoreVersion")
                val testRunner =
                    AndroidLibrary(name = "androidx.test:runner:$testRunnerVersion")
                val testRules =
                    AndroidLibrary(name = "androidx.test:rules:$testRunnerVersion")
                val testExtJunit =
                    AndroidLibrary(name = "androidx.test.ext:junit:$testExtJunitVersion")
            }
        }

        object MultiPlatform {
            const val mokoResources = "dev.icerock.moko:resources:$mokoResourcesVersion"
            const val mokoParcelize = "dev.icerock.moko:parcelize:$mokoParcelizeVersion"
            const val mokoGraphics = "dev.icerock.moko:graphics:$mokoGraphicsVersion"
        }

        object Jvm {
            const val kotlinPoet =
                "com.squareup:kotlinpoet:$kotlinPoetVersion"
            const val kotlinxSerialization =
                "org.jetbrains.kotlinx:kotlinx-serialization-runtime:$kotlinxSerializationVersion"
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
