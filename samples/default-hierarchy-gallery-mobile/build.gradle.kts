/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
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
        classpath(libs.kotlinGradlePlugin)
        classpath(libs.androidGradlePlugin)
        classpath(libs.composeJetBrainsPlugin)
        classpath(libs.composeCompilerPlugin)
    }
}

subprojects {
    plugins.withType<com.android.build.gradle.BasePlugin> {
        configure<BaseExtension> {
            defaultConfig.minSdk = 21
            compileSdkVersion(35)

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
        }
    }
    plugins.withType<KotlinPluginWrapper> {
        configure<KotlinProjectExtension> {
            jvmToolchain(17)
        }
    }
}
