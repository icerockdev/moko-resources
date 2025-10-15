/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.multiplatform")
 //   id("dev.icerock.mobile.multiplatform-resources")
}

android {
    namespace = "com.icerockdev.mpp"

    compileSdk = 33

    flavorDimensions += "type1" // Используйте то же имя измерения, что и в ваших flavors

    defaultConfig {
        minSdk = 16
        targetSdk = 33
    }

    defaultConfig {
        applicationId = "com.icerockdev.mpp"

        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        missingDimensionStrategy("type1", "dev") // Если :app собирает dev, и зависимость не имеет type1, используй 'dev' из type1
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    productFlavors {
        create("dev") {
            dimension = "type1"
        }
        create("prod") {
            dimension = "type1"
        }
    }
}

kotlin {
    androidTarget()
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation(project(":library"))
    commonMainImplementation(moko.resources)
}

//multiplatformResources {
//    resourcesPackage.set("com.icerockdev.library")
//}

tasks.matching { it.name == "packageAndroidMainResources" }
    .configureEach {
        dependsOn(tasks.named("generateMRandroidMain"))
    }