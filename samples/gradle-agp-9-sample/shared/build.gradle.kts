plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatfrom)
    id("dev.icerock.mobile.multiplatform-resources")
}

kotlin {
    androidLibrary {
        namespace = "com.gradle9sample.android.library"
        compileSdk = 36
        minSdk = 26

        withHostTest {
            isIncludeAndroidResources = true
        }

        withDeviceTest { }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
            export(moko.resources)
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(moko.resources)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(moko.resourcesTest)
            implementation(project(":shared:test-utils"))
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")

        languageVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0
        // Optional: Set jvmTarget
//        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}

multiplatformResources {
    resourcesPackage.set("app.gradle9sample.library")
}
