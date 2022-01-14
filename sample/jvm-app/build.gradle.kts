import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform") // kotlin("jvm") doesn't work well in IDEA/AndroidStudio (https://github.com/JetBrains/compose-jb/issues/22)
    id("org.jetbrains.compose")
    id("detekt-convention")
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
        commonMain {
            dependencies {
                implementation(projects.resources)
            }
        }
        named("jvmMain") {
            dependencies {
                implementation(projects.resourcesCompose)
                implementation(compose.desktop.currentOs)
                implementation(projects.sample.mppLibrary)
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
    multiplatformResourcesPackage = "com.icerockdev.app"
}
