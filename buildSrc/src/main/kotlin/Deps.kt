/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

object Deps {
    object Plugins {
        val androidApplication = PluginDesc(id = "com.android.application")
        val androidLibrary = PluginDesc(
            id = "com.android.library",
            module = "com.android.tools.build:gradle:${Versions.Plugins.android}"
        )

        val kotlinMultiplatform = PluginDesc(
            id = "org.jetbrains.kotlin.multiplatform",
            module = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.Plugins.kotlin}"
        )
        val kotlinKapt = PluginDesc(id = "kotlin-kapt")
        val kotlinAndroid = PluginDesc(id = "kotlin-android")
        val kotlinAndroidExtensions = PluginDesc(id = "kotlin-android-extensions")
        val kotlinSerialization = PluginDesc(id = "kotlin-serialization")

        val mobileMultiplatform = PluginDesc(id = "dev.icerock.mobile.multiplatform")

        val mokoResources = PluginDesc(
            id = "dev.icerock.mobile.multiplatform-resources",
            module = "dev.icerock.moko:resources-generator:${Versions.Plugins.mokoResources}"
        )

        val detekt = PluginDesc(id = "io.gitlab.arturbosch.detekt", version = Versions.detekt)
    }

    object Libs {
        object Android {
            val kotlinStdLib = AndroidLibrary(
                name = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}"
            )
            val appCompat = AndroidLibrary(
                name = "androidx.appcompat:appcompat:${Versions.Libs.Android.appCompat}"
            )

            object AndroidTest {
                val espressoCore = AndroidLibrary(
                    name = "androidx.test.espresso:espresso-core:${Versions.Libs.Android.AndroidTest.espressoCore}"
                )
                val testRunner = AndroidLibrary(
                    name = "androidx.test:runner:${Versions.Libs.Android.AndroidTest.testRunner}"
                )
                val testRules = AndroidLibrary(
                    name = "androidx.test:rules:${Versions.Libs.Android.AndroidTest.testRunner}"
                )
                val testExtJunit = AndroidLibrary(
                    name = "androidx.test.ext:junit:${Versions.Libs.Android.AndroidTest.testExtJunit}"
                )
            }
        }

        object MultiPlatform {
            val kotlinStdLib = MultiPlatformLibrary(
                android = Android.kotlinStdLib.name,
                common = "org.jetbrains.kotlin:kotlin-stdlib-common:${Versions.kotlin}"
            )
            val mokoResources = MultiPlatformLibrary(
                common = "dev.icerock.moko:resources:${Versions.Libs.MultiPlatform.mokoResources}",
                iosX64 = "dev.icerock.moko:resources-iosx64:${Versions.Libs.MultiPlatform.mokoResources}",
                iosArm64 = "dev.icerock.moko:resources-iosarm64:${Versions.Libs.MultiPlatform.mokoResources}"
            )
            val mokoParcelize = MultiPlatformLibrary(
                common = "dev.icerock.moko:parcelize:${Versions.Libs.MultiPlatform.mokoParcelize}",
                iosX64 = "dev.icerock.moko:parcelize-iosx64:${Versions.Libs.MultiPlatform.mokoParcelize}",
                iosArm64 = "dev.icerock.moko:parcelize-iosarm64:${Versions.Libs.MultiPlatform.mokoParcelize}"
            )
            val mokoGraphics = MultiPlatformLibrary(
                common = "dev.icerock.moko:graphics:${Versions.Libs.MultiPlatform.mokoGraphics}",
                iosX64 = "dev.icerock.moko:graphics-iosx64:${Versions.Libs.MultiPlatform.mokoGraphics}",
                iosArm64 = "dev.icerock.moko:graphics-iosarm64:${Versions.Libs.MultiPlatform.mokoGraphics}"
            )

        }

        object Jvm {
            const val kotlinPoet = "com.squareup:kotlinpoet:${Versions.Libs.Jvm.kotlinPoet}"
            const val kotlinxSerialization = "org.jetbrains.kotlinx:kotlinx-serialization-runtime:${Versions.Libs.Jvm.kotlinxSerialization}"
            const val apacheCommonsText = "org.apache.commons:commons-text:${Versions.Libs.Jvm.apacheCommonsText}"
            const val detektFormatting = "io.gitlab.arturbosch.detekt:detekt-formatting:${Versions.detekt}"
        }
    }
}
