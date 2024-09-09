buildscript {
    repositories {
        gradlePluginPortal()
    }

    dependencies {
        classpath("dev.icerock.moko:resources-generator:0.24.2")
    }
}

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    id("app.cash.paparazzi") version "1.3.4" apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
}
