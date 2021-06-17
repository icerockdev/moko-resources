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
    jvm()

    cocoapods {
        // Configure fields required by CocoaPods.
        summary = "Some description for a Kotlin/Native module"
        homepage = "Link to a Kotlin/Native module homepage"
    }

    // export correct artifact to use all classes of moko-resources directly from Swift
    targets.withType(org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget::class.java).all {
        binaries.withType(org.jetbrains.kotlin.gradle.plugin.mpp.Framework::class.java).all {
            export(project(":resources"))
        }
    }

    sourceSets {
        val commonMain by getting
        val commonTest by getting

        val clientMain by creating { dependsOn(commonMain) }
        val clientTest by creating { dependsOn(commonTest) }

        val iosMain by getting { dependsOn(clientMain) }
        val iosTest by getting { dependsOn(clientTest) }

        val macosMain by getting  { dependsOn(clientMain) }
        val macosTest by getting { dependsOn(clientTest) }

        val androidMain by getting  { dependsOn(clientMain) }
        val androidTest by getting { dependsOn(clientTest) }

        val jvmMain by getting
    }
}

dependencies {
    "clientMainApi"(libs.mokoResources)

    "androidTestImplementation"(libs.kotlinTestJUnit)
    "androidTestImplementation"(libs.testCore)
    "androidTestImplementation"(libs.robolectric)
    "commonTestImplementation"(libs.kotlinTest)
    "commonTestImplementation"(libs.kotlinTestAnnotations)
}

multiplatformResources {
    multiplatformResourcesPackage = "com.icerockdev.library"
    disableStaticFrameworkWarning = true
    multiplatformResourcesSourceSet = "clientMain"
}
