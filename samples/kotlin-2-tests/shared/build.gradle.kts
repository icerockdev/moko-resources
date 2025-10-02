plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.multiplatform)
    id("dev.icerock.mobile.multiplatform-resources")
}

kotlin {
    jvmToolchain(17)

    androidTarget()
    jvm()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    js(IR) {
        browser {
            testTask {
                useKarma {
                    useSourceMapSupport()
                    useChromeHeadless()
                }
            }
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                api(libs.coroutines.core)
                api(libs.moko.resources)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotest.assertions.core)
                implementation(libs.kotlin.test)

                implementation(libs.coroutines.test)
            }
        }

        androidMain.dependencies {
        }
        val androidUnitTest by getting {
            // must use an android test SourceSet!
            android.sourceSets.getByName("test").resources.srcDir("src/commonTest/resources")
            dependencies {
                implementation(libs.junit)
                implementation(libs.robolectric)
            }
        }

        jsMain.dependencies {
            implementation(devNpm("copy-webpack-plugin", "12.0.2"))
        }
        jsTest.dependencies {
            implementation(npm("karma-safarinative-launcher", "1.1.0"))
        }

        iosMain.dependencies {
        }

        jvmMain.dependencies {
        }
    }
}

multiplatformResources {
    resourcesPackage.set("template.composemultiplatform.shared")
    resourcesClassName.set("SharedRes")
}

android {
    namespace = "template.composemultiplatform.shared"
    compileSdk = 35
    defaultConfig {
        minSdk = 21
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}
