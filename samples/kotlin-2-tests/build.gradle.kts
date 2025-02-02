allprojects {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.plugin.serialization) apply false
    alias(libs.plugins.kotlin.plugin.compose) apply false
    alias(libs.plugins.kotlin.native.cocoapods) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.moko.resources) apply false
    alias(libs.plugins.paparazzi) apply false
    alias(libs.plugins.licenses) apply false
}
