/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("multiplatform-library-convention")
    id("dev.icerock.mobile.multiplatform.apple-framework")
    id("dev.icerock.mobile.multiplatform-resources")
    id("detekt-convention")
}

android {
    lintOptions {
        disable("ImpliedQuantity")
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    commonMainApi(projects.resources)
    commonMainApi(libs.mokoGraphics)
    commonMainImplementation(projects.sample.mppLibrary.nestedModule)

    commonTestImplementation(projects.resourcesTest)
    commonTestImplementation(projects.sample.testUtils)
}

multiplatformResources {
    multiplatformResourcesPackage = "com.icerockdev.library"
}

framework {
    export(projects.resources)
    export(libs.mokoGraphics)
}

tasks.register("debugFatFramework", dev.icerock.gradle.tasks.FatFrameworkWithResourcesTask::class) {
    baseName = "multiplatform"

    val targets = mapOf(
        "iosX64" to kotlin.targets.getByName<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>("iosX64"),
        "iosArm64" to kotlin.targets.getByName<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>("iosArm64"),
        "iosSimulatorArm64" to kotlin.targets.getByName<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>("iosSimulatorArm64")
    )

    from(
        targets.toList().map {
            it.second.binaries.getFramework("MultiPlatformLibrary", org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType.DEBUG)
        }
    )
}
