/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("org.jetbrains.kotlin.jvm") version ("1.4.31")
    id("org.gradle.maven-publish")
    id("io.gitlab.arturbosch.detekt") version ("1.15.0")
}

buildscript {
    repositories {
        mavenCentral()
        google()

        maven { url = uri("https://dl.bintray.com/icerockdev/plugins") }
    }
    dependencies {
        classpath("gradle:resources-deps:1")
    }
}

repositories {
    mavenCentral()
    google()
    jcenter()
}

group = "dev.icerock.moko"
version = Deps.mokoResourcesVersion

dependencies {
    implementation(gradleKotlinDsl())
    compileOnly(Deps.Libs.Jvm.kotlinGradlePlugin)
    compileOnly(Deps.Libs.Jvm.androidGradlePlugin)
    implementation(Deps.Libs.Jvm.kotlinPoet)
    implementation(Deps.Libs.Jvm.kotlinxSerialization)
    implementation(Deps.Libs.Jvm.apacheCommonsText)
    implementation(Deps.Libs.Jvm.kotlinCompilerEmbeddable)
    detektPlugins(Deps.Libs.Jvm.detektFormatting)
}

publishing {
    repositories.maven("https://api.bintray.com/maven/icerockdev/plugins/moko-resources-generator/;publish=1") {
        name = "bintray"

        credentials {
            username = System.getProperty("BINTRAY_USER")
            password = System.getProperty("BINTRAY_KEY")
        }
    }
    publications {
        register("maven", MavenPublication::class) {
            from(components["java"])
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}
