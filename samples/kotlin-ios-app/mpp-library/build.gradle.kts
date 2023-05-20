/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("dev.icerock.mobile.multiplatform-resources")
}

kotlin {
    ios()
    iosSimulatorArm64()
    
    sourceSets {
        val iosMain by getting {
            dependencies {
                api(moko.resources)
            }
        }
        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }
    }
}

multiplatformResources {
    multiplatformResourcesPackage = "com.icerockdev.library"
    multiplatformResourcesSourceSet = "iosMain"
}

tasks.matching { it.name == "compileIosMainKotlinMetadata" }
    .configureEach { dependsOn("generateMRiosMain") }
