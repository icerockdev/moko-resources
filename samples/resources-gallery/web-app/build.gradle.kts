plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("dev.icerock.mobile.multiplatform-resources")
}

kotlin {
    js(IR) {
        browser()
        binaries.executable()
    }
    sourceSets {
        commonMain {
            dependencies {
                implementation(moko.resources)
                implementation(project(":mpp-library"))

                implementation(compose.html.core)
                implementation(compose.runtime)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
            }
        }
    }
}

multiplatformResources {
    resourcesPackage.set("com.icerockdev.app")
}
