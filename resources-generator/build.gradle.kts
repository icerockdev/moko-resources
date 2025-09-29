/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    id("org.jetbrains.kotlin.jvm") version ("1.9.25")
    id("detekt-convention")
    id("publication-convention")
    id("com.gradle.plugin-publish") version ("1.2.0")
    id("java-gradle-plugin")
    kotlin("plugin.serialization") version ("1.9.25")
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
        compilerOptions.languageVersion.set(KotlinVersion.KOTLIN_1_9)
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

project.plugins.withId("com.android.kotlin.multiplatform.library") {
    val androidComponents = project.extensions.getByType(
        com.android.build.api.variant.AndroidComponentsExtension::class.java
    )

    androidComponents.onVariants { variant ->
        val capName = variant.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

        // задача генерации создаётся библиотекой/плагином
        val generateTask = project.tasks.named("generateMRandroidMain")

        // связываем упаковку ресурсов конкретного варианта с генерацией
        project.tasks.matching { it.name == "package${capName}Resources" }
            .configureEach {
                dependsOn(generateTask)
            }
    }
}