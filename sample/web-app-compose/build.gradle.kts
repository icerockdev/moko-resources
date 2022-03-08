import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackCssSupport

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("detekt-convention")
    id("dev.icerock.mobile.multiplatform-resources")
}

kotlin {
    js(IR) {
        browser {
            webpackTask {
                cssSupport.enabled = true
            }
        }
        binaries.executable()
    }
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.resources)
                implementation(projects.sample.mppLibrary)

                implementation(compose.web.core)
                implementation(compose.runtime)
            }
        }
    }
}

multiplatformResources {
    multiplatformResourcesPackage = "com.icerockdev.app"
}
