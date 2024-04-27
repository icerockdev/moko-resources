plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("dev.icerock.mobile.multiplatform-resources")
}

group = "me.amikhailov"
version = "1.0"

kotlin {
    jvm("desktop") {
        compilations.all {
            kotlinOptions.jvmTarget = "17"
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)

                api(moko.resources)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val desktopMain by getting {
            dependencies {
                api(compose.preview)
                implementation(compose.desktop.currentOs)
            }
        }
        val desktopTest by getting
    }
}

multiplatformResources {
    resourcesPackage.set("com.icerockdev.app")
}

compose {
    desktop {
        application {
            mainClass = "me.amikhailov.common.MainKt"
            nativeDistributions {
                targetFormats(
                    org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg,
                    org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                    org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb
                )
                packageName = "MokoDesktopApp"
                version = "1.0.0"
            }
        }
    }
}
