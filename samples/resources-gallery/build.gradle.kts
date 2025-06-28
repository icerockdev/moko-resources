/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import com.android.build.gradle.BaseExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
        gradlePluginPortal()
    }
    dependencies {
        classpath(moko.resourcesGradlePlugin)
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.0")
        classpath("org.jetbrains.compose:compose-gradle-plugin:1.8.2")
        classpath("com.android.tools.build:gradle:8.11.0")
    }
}

plugins {
    kotlin("plugin.compose") version "2.2.0" apply false
}

subprojects {
    plugins.withType<com.android.build.gradle.BasePlugin> {
        configure<BaseExtension> {
            defaultConfig.minSdkVersion(19)
            compileSdkVersion(35)

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_21
                targetCompatibility = JavaVersion.VERSION_21
            }
        }
    }
    plugins.withType<KotlinPluginWrapper> {
        configure<KotlinProjectExtension> {
            jvmToolchain(21)
        }
    }
}
