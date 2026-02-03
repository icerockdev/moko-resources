/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
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
            iosX64()
            iosArm64()
            iosSimulatorArm64()

            explicitApi()
        }
    }
}

android {
    namespace = "com.icerockdev.library"

    testOptions.unitTests.isIncludeAndroidResources = true

    lint.disable.add("ImpliedQuantity")
    lint.disable.add("MissingTranslation")
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

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

dependencies {
    commonMainApi(moko.resources)

    commonTestImplementation(moko.resourcesTest)
    commonTestImplementation(project(":mpp-library:test-utils"))
}

multiplatformResources {
    resourcesPackage.set("com.icerockdev.library")
}
