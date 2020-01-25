/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

object Deps {
    object Plugins {
        const val android =
            "com.android.tools.build:gradle:${Versions.Plugins.android}"
        const val kotlin =
            "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.Plugins.kotlin}"
        const val mokoResources =
            "dev.icerock.moko:resources-generator:${Versions.Plugins.mokoResources}"
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
                    name = "androidx.test.espresso:espresso-core:${Versions.Libs.Android.espressoCore}"
                )
                val testRunner = AndroidLibrary(
                    name = "androidx.test:runner:${Versions.Libs.Android.testRunner}"
                )
                val testRules = AndroidLibrary(
                    name = "androidx.test:rules:${Versions.Libs.Android.testRunner}"
                )
                val testExtJunit = AndroidLibrary(
                    name = "androidx.test.ext:junit:${Versions.Libs.Android.testExtJunit}"
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
        }

        object Jvm {
            const val kotlinPoet = "com.squareup:kotlinpoet:${Versions.Libs.Jvm.kotlinPoet}"
            const val apacheCommonsText = "org.apache.commons:commons-text:${Versions.Libs.Jvm.apacheCommonsText}"
        }
    }

    val plugins: Map<String, String> = mapOf(
        "com.android.application" to Plugins.android,
        "com.android.library" to Plugins.android,
        "org.jetbrains.kotlin.multiplatform" to Plugins.kotlin,
        "kotlin-kapt" to Plugins.kotlin,
        "kotlin-android" to Plugins.kotlin,
        "dev.icerock.mobile.multiplatform-resources" to Plugins.mokoResources
    )
}