plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("dev.icerock.mobile.multiplatform-resources")
}

kotlin {
    jvmToolchain(17)

    androidLibrary {
        namespace = "dev.icerock.moko.resources.androidkmp"
        compileSdk = 35
        minSdk = 21
    }

    jvm()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            api(moko.resources)
        }
    }
}

multiplatformResources {
    resourcesPackage.set("dev.icerock.moko.resources.androidkmp")
}
