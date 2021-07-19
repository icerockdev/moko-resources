plugins {
    id("android-app-convention")
    id("dev.icerock.mobile.multiplatform-resources")
    kotlin(module = "android")
}

dependencies {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
    }
    implementation(dependencyNotation = "dev.icerock.moko:resources:0.16.2")
    implementation(dependencyNotation = "androidx.appcompat:appcompat:1.4.0-alpha03")
    implementation(dependencyNotation = "androidx.compose.ui:ui:1.0.0-rc02")
    implementation(dependencyNotation = "androidx.compose.ui:ui-tooling:1.0.0-rc02")
    implementation(dependencyNotation = "androidx.compose.ui:ui-tooling-preview:1.0.0-rc02")
    implementation(dependencyNotation = "androidx.compose.runtime:runtime:1.0.0-rc02")
    implementation(dependencyNotation = "androidx.compose.material:material:1.0.0-rc02")
    implementation(dependencyNotation = "androidx.compose.foundation:foundation:1.0.0-rc02")
    implementation(dependencyNotation = "androidx.activity:activity-compose:1.3.0-rc02")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
}

android {
    compileSdk = 31

    defaultConfig {
        minSdk = 21
        targetSdk = 31
        versionCode = 1
        versionName = "1.0"
        applicationId = "dev.arturmavl.mrfjci"
    }

    buildFeatures {
        compose = true
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
        freeCompilerArgs = freeCompilerArgs +
                "-Xopt-in=kotlin.RequiresOptIn" +
                "-Xexplicit-api=strict"
    }

    composeOptions {
        kotlinCompilerVersion = "1.5.10"
        kotlinCompilerExtensionVersion = "1.0.0-beta08"
    }
}