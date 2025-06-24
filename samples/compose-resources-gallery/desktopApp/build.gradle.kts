import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("dev.icerock.mobile.multiplatform-resources")
}

kotlin {
    jvm()
    js(IR) {
        browser {
            useCommonJs()
        }
        binaries.executable()
    }
    wasmJs {
        browser()
        binaries.executable()
    }
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":shared"))
            }
        }
        jvmMain {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "KotlinMultiplatformComposeDesktopApplication"
            packageVersion = "1.0.0"
        }
    }
}

multiplatformResources {
    resourcesPackage.set("com.icerockdev.app")
}
