plugins {
    id("com.android.application")
    id("android-base-convention")
}

android {
    dexOptions {
        javaMaxHeapSize = "2g"
    }

    defaultConfig.vectorDrawables.useSupportLibrary = true

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
        getByName("debug") {
            isDebuggable = true
            applicationIdSuffix = ".debug"
        }
    }

    packagingOptions {
        exclude("META-INF/*.kotlin_module")
    }
}
