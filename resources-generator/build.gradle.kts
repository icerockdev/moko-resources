/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("org.jetbrains.kotlin.jvm") version ("1.8.10")
    id("detekt-convention")
    id("publication-convention")
    id("com.gradle.plugin-publish") version ("1.2.0")
    id("java-gradle-plugin")
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
}

java {
    withJavadocJar()
    withSourcesJar()
}

kotlin {
    jvmToolchain(11)
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
