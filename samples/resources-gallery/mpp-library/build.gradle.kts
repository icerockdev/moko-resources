/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("dev.icerock.mobile.multiplatform-resources")
}

allprojects {
    this.plugins.withId("org.jetbrains.kotlin.multiplatform") {
        kotlin {
            android()
            ios()
            iosSimulatorArm64()
            jvm()
            macosX64()
            macosArm64()
            js(IR) { browser() }

            explicitApi()

            sourceSets {
                val iosMain by getting
                val iosSimulatorArm64Main by getting {
                    dependsOn(iosMain)
                }

                val macosMain by creating {
                    dependsOn(commonMain.get())
                }
                val macosX64Main by getting {
                    dependsOn(macosMain)
                }
                val macosArm64Main by getting {
                    dependsOn(macosMain)
                }
            }
        }
    }
}

android {
    namespace = "com.icerockdev.library"

    testOptions.unitTests.isIncludeAndroidResources = true

    lint.disable.add("ImpliedQuantity")
}

kotlin {
    val xcFramework = XCFramework("MultiPlatformLibrary")

    targets.withType(org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget::class)
        .matching { it.konanTarget.family == org.jetbrains.kotlin.konan.target.Family.IOS }
        .configureEach {
            binaries.withType(org.jetbrains.kotlin.gradle.plugin.mpp.Framework::class)
                .configureEach {
                    this.export(moko.resources)
                    xcFramework.add(this)
                }
        }
}

dependencies {
    commonMainApi(moko.resources)
    commonMainImplementation(project(":mpp-library:nested-module"))
    commonMainImplementation(project(":mpp-library:empty-module"))

    commonTestImplementation(moko.resourcesTest)
    commonTestImplementation(project(":mpp-library:test-utils"))
}

multiplatformResources {
    multiplatformResourcesPackage = "com.icerockdev.library"
}
