plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("detekt-convention")
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
                implementation(projects.resources)
                implementation(projects.sample.mppLibrary)

                implementation(compose.web.core)
            }
        }
    }
}

multiplatformResources {
    multiplatformResourcesPackage = "com.icerockdev.app"
}
