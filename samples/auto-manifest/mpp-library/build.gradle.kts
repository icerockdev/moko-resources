plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("dev.icerock.mobile.multiplatform-resources")
    id("com.gradleup.auto.manifest")
}

android {
    compileSdk = 33

    defaultConfig {
        minSdk = 16
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    androidTarget()
}

dependencies {
    commonMainApi(moko.resources)
}

multiplatformResources {
    resourcesPackage.set("com.icerockdev.library")
}

autoManifest {
    packageName.set("com.icerockdev.library")
}
