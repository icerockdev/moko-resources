/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("dev.icerock.mobile.multiplatform-resources")
    id("org.jetbrains.kotlin.native.cocoapods")
}

android {
    compileSdkVersion(Versions.Android.compileSdk)

    defaultConfig {
        minSdkVersion(Versions.Android.minSdk)
        targetSdkVersion(Versions.Android.targetSdk)
    }

    lintOptions {
        disable("ImpliedQuantity")
    }
}

// CocoaPods requires the podspec to have a version.
version = "1.0"

kotlin {
    android()
    ios()

    cocoapods {
        // Configure fields required by CocoaPods.
        summary = "Some description for a Kotlin/Native module"
        homepage = "Link to a Kotlin/Native module homepage"
    }

    // export correct artifact to use all classes of moko-resources directly from Swift
    targets.configureEach {
        if (this !is org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget) return@configureEach

        val arch = when (this.konanTarget) {
            org.jetbrains.kotlin.konan.target.KonanTarget.IOS_ARM64 -> "iosarm64"
            org.jetbrains.kotlin.konan.target.KonanTarget.IOS_X64 -> "iosx64"
            else -> throw IllegalArgumentException()
        }

        this.binaries.configureEach {
            if (this is org.jetbrains.kotlin.gradle.plugin.mpp.Framework) {
                this.export("dev.icerock.moko:resources-$arch:${Versions.Libs.MultiPlatform.mokoResources}")
            }
        }
    }
}

dependencies {
    commonMainImplementation("org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}")
    commonMainApi("dev.icerock.moko:resources:${Versions.Libs.MultiPlatform.mokoResources}")
}

multiplatformResources {
    multiplatformResourcesPackage = "com.icerockdev.library"
    disableStaticFrameworkWarning = true
}

