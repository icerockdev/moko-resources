plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
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

                implementation(compose.html.core)
            }
        }
    }
}

multiplatformResources {
    resourcesPackage.set("com.icerockdev.app")
}
