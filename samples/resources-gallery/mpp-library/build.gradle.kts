/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.native.cocoapods")
    id("dev.icerock.mobile.multiplatform-resources")
}

allprojects {
    plugins.withId("org.jetbrains.kotlin.multiplatform") {
        kotlin {
            androidTarget()
            ios()
            iosSimulatorArm64()
            jvm()
            macosX64()
            macosArm64()
            tvosX64()
            tvosArm64()
            tvosSimulatorArm64()
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

                val tvosMain by creating {
                    dependsOn(commonMain.get())
                }
                val tvosX64Main by getting {
                    dependsOn(tvosMain)
                }
                val tvosArm64Main by getting {
                    dependsOn(tvosMain)
                }
            }
        }
    }
}

android {
    namespace = "com.icerockdev.library"

    testOptions.unitTests.isIncludeAndroidResources = true

    lint.disable.add("ImpliedQuantity")
    lint.disable.add("MissingTranslation")
    lint.disable.add("MissingQuantity")
}

kotlin {
    cocoapods {
        version = "1.0"
        summary = "Some description for a Kotlin/Native module"
        homepage = "Link to a Kotlin/Native module homepage"

        framework {
            baseName = "MultiPlatformLibrary"

            export(moko.resources)
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
    resourcesPackage.set("com.icerockdev.library")
}
