import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform") // kotlin("jvm") doesn't work well in IDEA/AndroidStudio (https://github.com/JetBrains/compose-jb/issues/22)
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("dev.icerock.mobile.multiplatform-resources")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

kotlin {
    jvm()
    sourceSets {
        named("jvmMain") {
            dependencies {
                implementation(moko.resources)
                implementation(compose.desktop.currentOs)
                implementation(project(":mpp-library"))
            }
        }
    }
}

compose {
    desktop {
        application {
            mainClass = "com.icerockdev.desktop.MainKt"
            nativeDistributions {
                targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
                packageName = "MokoDesktopApp"
                version = "1.0.0"
            }
        }
    }
}

multiplatformResources {
    resourcesClassName.set("AppMR")
    resourcesPackage.set("com.icerockdev.app")
    resourcesSourceSets {
        getByName("jvmMain").srcDirs(
            File(projectDir, "customResources")
        )
    }
}
