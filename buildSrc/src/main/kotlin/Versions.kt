/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

object Versions {
    object Android {
        const val compileSdk = 28
        const val targetSdk = 28
        const val minSdk = 16
    }

    const val kotlin = "1.3.72"
    const val detekt = "1.7.4"

    private const val mokoResources = "0.12.0"

    object Plugins {
        const val android = "3.6.2"

        const val kotlin = Versions.kotlin
        const val mokoResources = Versions.mokoResources
    }

    object Libs {
        object Android {
            const val appCompat = "1.1.0"

            object AndroidTest {
                const val espressoCore = "3.2.0"
                const val testRunner = "1.2.0"
                const val testExtJunit = "1.1.1"
            }
        }

        object MultiPlatform {
            const val mokoResources = Versions.mokoResources
            const val mokoParcelize = "0.3.0"
            const val mokoGraphics = "0.3.0"
        }

        object Jvm {
            const val apacheCommonsText = "1.3"
            const val kotlinPoet = "1.6.0"
            const val kotlinxSerialization = "0.20.0"
        }
    }
}
