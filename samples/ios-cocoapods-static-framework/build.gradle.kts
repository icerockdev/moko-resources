import dev.icerock.gradle.tasks.GenerateMultiplatformResourcesTask

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    kotlin("multiplatform").apply(false)
    id("org.jetbrains.compose").apply(false)
}

buildscript {
    repositories {
        mavenLocal()
        gradlePluginPortal()
    }


    dependencies {
        classpath(moko.resourcesGradlePlugin)
    }
}
