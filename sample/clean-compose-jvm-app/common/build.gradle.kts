import org.jetbrains.compose.compose

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
            kotlinOptions.jvmTarget = "11"
        }
        withJava()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)

                val resourcesVersion: String = libs.versions.mokoResourcesVersion.get()
                api("dev.icerock.moko:resources:$resourcesVersion")
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
            }
        }
        val desktopTest by getting
    }
}

multiplatformResources {
    multiplatformResourcesPackage = "com.icerockdev.app"
}
