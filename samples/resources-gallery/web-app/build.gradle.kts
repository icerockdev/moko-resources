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

                implementation(compose.web.core)
                implementation(compose.runtime)
            }
        }
    }
}

multiplatformResources {
    multiplatformResourcesPackage = "com.icerockdev.app"
}
