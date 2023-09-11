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

                // TODO()
                // implementation(compose.html.core)
                // implementation(compose.runtime)
                implementation("org.jetbrains.compose.web:web-core:1.4.0")
                runtimeOnly("org.jetbrains.compose.runtime:runtime:1.4.0")

            }
        }
    }
}

multiplatformResources {
    resourcesPackage.set("com.icerockdev.app")
}
