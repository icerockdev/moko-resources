buildscript {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
        mavenLocal()
    }
    dependencies {
        classpath(moko.resourcesGradlePlugin)
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.0")
        classpath("org.jetbrains.compose:compose-gradle-plugin:1.8.2")
    }
}

plugins {
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.0" apply false
}
