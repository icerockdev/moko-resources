/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("io.gitlab.arturbosch.detekt") version("1.15.0") apply(false)
}

buildscript {
    repositories {
        mavenCentral()
        google()

        gradlePluginPortal()
    }
    dependencies {
        classpath("dev.icerock.moko:resources-generator:0.15.1")

        classpath("dev.icerock:mobile-multiplatform:0.9.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.10")
        classpath("com.android.tools.build:gradle:4.2.1")
    }
}

allprojects {
    repositories {
        mavenCentral()
        google()
    }

    apply(plugin = "io.gitlab.arturbosch.detekt")

    configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        input.setFrom("src/commonMain/kotlin", "src/androidMain/kotlin", "src/iosMain/kotlin")
    }

    dependencies {
        //"detektPlugins"(libs.detektFormatting)
        "detektPlugins"("io.gitlab.arturbosch.detekt:detekt-formatting:1.15.0")
    }

    plugins.withId("com.android.library") {
        configure<com.android.build.gradle.LibraryExtension> {
            compileSdkVersion(libs.versions.compileSdk.get().toInt())

            defaultConfig {
                minSdkVersion(libs.versions.minSdk.get().toInt())
                targetSdkVersion(libs.versions.targetSdk.get().toInt())
            }
        }
    }
}

tasks.register("clean", Delete::class).configure {
    group = "build"
    delete(rootProject.buildDir)
}
