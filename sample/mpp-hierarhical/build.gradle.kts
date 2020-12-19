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
    lintOptions {
        disable("ImpliedQuantity")
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

// CocoaPods requires the podspec to have a version.
version = "1.0"

kotlin {
    android()
    ios()
    macosX64("macos")

    cocoapods {
        // Configure fields required by CocoaPods.
        summary = "Some description for a Kotlin/Native module"
        homepage = "Link to a Kotlin/Native module homepage"
    }

    // export correct artifact to use all classes of moko-resources directly from Swift
    targets.configureEach {
        if (this !is org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget) return@configureEach

        this.binaries.configureEach {
            if (this is org.jetbrains.kotlin.gradle.plugin.mpp.Framework) {
                this.export(project(":resources"))
            }
        }
    }
}

dependencies {
    commonMainApi(Deps.Libs.MultiPlatform.mokoResources)

    androidTestImplementation(Deps.Libs.Android.Tests.kotlinTestJUnit)
    androidTestImplementation(Deps.Libs.Android.Tests.testCore)
    androidTestImplementation(Deps.Libs.Android.Tests.robolectric)
    commonTestImplementation(Deps.Libs.MultiPlatform.Tests.kotlinTest)
    commonTestImplementation(Deps.Libs.MultiPlatform.Tests.kotlinTestAnnotations)
}

multiplatformResources {
    multiplatformResourcesPackage = "com.icerockdev.library"
    disableStaticFrameworkWarning = true
}

