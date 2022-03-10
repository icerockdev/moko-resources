/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    id("com.android.library")
    id("android-base-convention")
    id("org.jetbrains.kotlin.multiplatform")
    id("dev.icerock.mobile.multiplatform-resources")
    id("org.jetbrains.kotlin.native.cocoapods")
    id("detekt-convention")
}

android {
    lint {
        disable += "ImpliedQuantity"
        ignoreTestSources = true
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
    iosSimulatorArm64()
    macosX64("macos")
    jvm()

    cocoapods {
        // Configure fields required by CocoaPods.
        summary = "Some description for a Kotlin/Native module"
        homepage = "Link to a Kotlin/Native module homepage"

        xcodeConfigurationToNativeBuildType["Stage"] = NativeBuildType.RELEASE
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
                implementation(libs.coroutinesTest)
            }
        }

        val clientMain by creating {
            dependsOn(commonMain)

            dependencies {
                api(projects.resources)
            }
        }
        val clientTest by creating {
            dependsOn(commonTest)
            dependencies {
                implementation(projects.sample.testUtils)
            }
        }

        val iosMain by getting { dependsOn(clientMain) }
        val iosTest by getting { dependsOn(clientTest) }

        val iosSimulatorArm64Main by getting { dependsOn(iosMain) }
        val iosSimulatorArm64Test by getting { dependsOn(iosTest) }

        val macosMain by getting { dependsOn(clientMain) }
        val macosTest by getting { dependsOn(clientTest) }

        val androidMain by getting { dependsOn(clientMain) }
        val androidTest by getting {
            dependsOn(clientTest)

            dependencies {
                implementation(libs.kotlinTestJUnit)
                implementation(libs.testCore)
                implementation(libs.robolectric)
            }
        }

        val jvmMain by getting {
            dependsOn(clientMain)
        }
        val jvmTest by getting {
            dependsOn(clientTest)
        }
    }

    val xcFramework = XCFramework("MPL")
    targets.withType(org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget::class)
        .matching { it.konanTarget.family == org.jetbrains.kotlin.konan.target.Family.IOS }
        .configureEach {
            binaries.withType(org.jetbrains.kotlin.gradle.plugin.mpp.Framework::class)
                .matching { it.buildType == NativeBuildType.RELEASE } // static framework not produce dSYMs and here no reasons for Debug XCFramework
                .configureEach { xcFramework.add(this) }
        }
}

multiplatformResources {
    multiplatformResourcesPackage = "com.icerockdev.library"
    disableStaticFrameworkWarning = true
    multiplatformResourcesSourceSet = "clientMain"
}
