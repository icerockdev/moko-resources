plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("dev.icerock.mobile.multiplatform-resources")
    id("com.gradleup.auto.manifest")
}

android {
    compileSdk = 33

    defaultConfig {
        minSdk = 21
    }
}

kotlin {
    android()
}

dependencies {
    commonMainApi(moko.resources)
}

multiplatformResources {
    multiplatformResourcesPackage = "com.icerockdev.library"
}

autoManifest {
    packageName.set("com.icerockdev.library")
}

afterEvaluate {
    tasks.withType<JavaCompile>().configureEach {
        sourceCompatibility = JavaVersion.VERSION_17.toString()
        targetCompatibility = JavaVersion.VERSION_17.toString()
    }
}
