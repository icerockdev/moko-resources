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

                implementation(compose.html.core)
            }
        }
    }
}

compose.experimental {
    web.application {}
}

multiplatformResources {
    resourcesPackage.set("com.icerockdev.app")
}
