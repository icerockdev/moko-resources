/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("com.android.library")
    id("android-base-convention")
    id("org.jetbrains.kotlin.multiplatform")
    id("dev.icerock.mobile.multiplatform-resources")
    id("org.jetbrains.kotlin.native.cocoapods")
    id("detekt-convention")
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
            export(projects.resources)
        }
    }

    sourceSets {
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlinTest)
                implementation(libs.kotlinTestAnnotations)
            }
        }

        val clientMain by creating { 
            dependsOn(commonMain) 

            dependencies {
                api(projects.resources)
            }
        }
        val clientTest by creating { dependsOn(commonTest) }

        val iosMain by getting { dependsOn(clientMain) }
        val iosTest by getting { dependsOn(clientTest) }

        val macosMain by getting  { dependsOn(clientMain) }
        val macosTest by getting { dependsOn(clientTest) }

        val androidMain by getting  { dependsOn(clientMain) }
        val androidTest by getting { 
            dependsOn(clientTest) 

            dependencies {
                implementation(libs.kotlinTestJUnit)
                implementation(libs.testCore)
                implementation(libs.robolectric)
            }
        }

        val jvmMain by getting
    }
}

multiplatformResources {
    multiplatformResourcesPackage = "com.icerockdev.library"
    disableStaticFrameworkWarning = true
    multiplatformResourcesSourceSet = "clientMain"
}
