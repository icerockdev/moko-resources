/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    id("org.jetbrains.kotlin.jvm") version ("1.9.0")
    id("detekt-convention")
    id("publication-convention")
    id("com.gradle.plugin-publish") version ("1.2.0")
    id("java-gradle-plugin")
    kotlin("plugin.serialization") version ("1.9.0")
}

group = "dev.icerock.moko"
version = moko.versions.resourcesVersion.get()

dependencies {
    implementation(gradleKotlinDsl())
    compileOnly(libs.kotlinGradlePlugin)
    compileOnly(libs.androidGradlePlugin)
    compileOnly(libs.kotlinCompilerEmbeddable)
    compileOnly(libs.androidSdkCommon)
    implementation(libs.kotlinPoet)
    implementation(libs.kotlinxSerialization)
    implementation(libs.apacheCommonsText)
    implementation(libs.commonsCodec)

    testImplementation(kotlin("test-junit"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11

    withJavadocJar()
    withSourcesJar()
}

kotlin {
    jvmToolchain(11)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile>()
    .configureEach {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_11)
        compilerOptions.languageVersion.set(KotlinVersion.KOTLIN_1_7)
    }

gradlePlugin {
    plugins {
        create("multiplatform-resources") {
            id = "dev.icerock.mobile.multiplatform-resources"
            implementationClass = "dev.icerock.gradle.MultiplatformResourcesPlugin"

            displayName = "MOKO resources generator plugin"
            description = "Plugin to provide access to the resources on iOS & Android"
            tags.set(listOf("moko-resources", "moko", "kotlin", "kotlin-multiplatform"))
        }
    }

    website.set("https://github.com/icerockdev/moko-resources")
    vcsUrl.set("https://github.com/icerockdev/moko-resources")
}
