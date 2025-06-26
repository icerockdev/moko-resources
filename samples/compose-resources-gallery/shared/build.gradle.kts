import org.jetbrains.kotlin.gradle.tasks.DummyFrameworkTask

plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("dev.icerock.mobile.multiplatform-resources")
}

version = "1.0-SNAPSHOT"

kotlin {
    androidTarget()

    iosArm64()
    iosX64()
    iosSimulatorArm64()

    macosArm64()
    macosX64()

    jvm("desktop")
    js(IR) {
        browser()
    }
    wasmJs {
        browser()
        binaries.executable()
    }

    cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        ios.deploymentTarget = "15.2"
        podfile = project.file("../iosApp/Podfile")
        framework {
            baseName = "shared"
            isStatic = true
        }
        // TODO move to gradle plugin
        extraSpecAttributes["resource"] = "'build/cocoapods/framework/shared.framework/*.bundle'"
    }

    @Suppress("UNUSED_VARIABLE")
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                api(moko.resources)
                api(moko.resourcesCompose)
            }
        }
        val androidMain by getting {
            dependencies {
                api("androidx.activity:activity-compose:1.7.2")
                api("androidx.appcompat:appcompat:1.6.1")
                api("androidx.core:core-ktx:1.10.1")
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.common)
            }
        }
        val jsMain by getting {
            dependencies {
                implementation(compose.html.core)
            }
        }
    }
}

android {
    compileSdk = 34
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    lint {
        disable.add("MissingTranslation")
    }

    namespace = "com.myapplication.common"
}

multiplatformResources {
    resourcesPackage.set("com.icerockdev.library")
}

// TODO move to gradle plugin
tasks.withType<DummyFrameworkTask>().configureEach {
    @Suppress("ObjectLiteralToLambda")
    doLast(object : Action<Task> {
        override fun execute(task: Task) {
            task as DummyFrameworkTask

            val frameworkDir: File = task.outputFramework.get().asFile

            // TODO here we should fill list from local gradle modules
            //  AND from external dependencies with bundles
            //  to fill full list of bundles
            listOf(
                "compose-resources-gallery:shared.bundle"
            ).forEach { bundleName ->
                val bundleDir = File(frameworkDir, bundleName)
                bundleDir.mkdir()
                File(bundleDir, "dummyFile").writeText("dummy")
            }
        }
    })
}
