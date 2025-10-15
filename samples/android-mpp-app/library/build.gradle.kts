import com.android.build.api.variant.KotlinMultiplatformAndroidComponentsExtension

/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
 //   id("com.android.library")
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("dev.icerock.mobile.multiplatform-resources")
}

//android {
//    namespace = "com.icerockdev.mpplibrary"
//
//    compileSdk = 33
//
//    defaultConfig {
//        minSdk = 16
//        targetSdk = 33
//    }
//
//    compileOptions {
//        sourceCompatibility = JavaVersion.VERSION_17
//        targetCompatibility = JavaVersion.VERSION_17
//    }
//}


kotlin {
   // androidTarget()
    jvm()

    androidLibrary {
        namespace = "com.icerockdev.mpplibrary"
        compileSdk = 33
        minSdk = 16
        androidResources.enable = true
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
}

dependencies {
    commonMainImplementation(moko.resources)
}

multiplatformResources {
    resourcesPackage.set("com.icerockdev.mpplibrary")
}
