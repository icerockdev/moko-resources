plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    kotlin("jvm") apply false
    kotlin("multiplatform") apply false
    kotlin("android") apply false
    id("com.android.application") apply false
    id("com.android.library") apply false
    id("app.cash.paparazzi") version "1.3.5" apply false
    id("org.jetbrains.compose") apply false
}

buildscript {
    repositories {
        mavenLocal()
    }
    dependencies {
        classpath(moko.resourcesGradlePlugin)
    }
}
