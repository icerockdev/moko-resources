import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("dev.icerock.mobile.multiplatform-resources")
}

kotlin {
    js(IR) {
        browser {
            useCommonJs()
        }
        binaries.executable()
    }
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":shared"))

                implementation(compose.web.core)
            }
        }
    }
}

compose.experimental {
    web.application {}
}

multiplatformResources {
    multiplatformResourcesPackage = "com.icerockdev.app"
}
