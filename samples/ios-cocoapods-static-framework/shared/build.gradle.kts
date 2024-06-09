plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("org.jetbrains.compose")
    id("dev.icerock.mobile.multiplatform-resources")
}

kotlin {
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        version = "1.0.0"
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        ios.deploymentTarget = "15"
        podfile = project.file("../iosApp/Podfile")
        framework {
            baseName = "SharedKit"
            binaryOptions["bundleId"] = "com.share.resources.module"

            isStatic = true
            export(moko.resources)
            export("dev.icerock.moko:graphics:0.9.0")
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)
                api(moko.resources)
                api(moko.resourcesCompose)
            }
        }
    }
}

multiplatformResources {
    resourcesPackage.set("com.share.resources.module")
}
