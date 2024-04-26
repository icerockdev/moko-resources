/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("multiplatform-library-convention")
    id("multiplatform-android-publish-convention")
    id("apple-main-convention")
    id("kotlin-parcelize")
    id("detekt-convention")
    id("javadoc-stub-convention")
    id("publication-convention")
}

kotlin {
    sourceSets {
        getByName("jsMain") {
            dependencies {
                api(npm("bcp-47", "2.1.0"))
                api(npm("@messageformat/core", "3.1.0"))
                api(npm("mini-css-extract-plugin", "2.7.5"))
                api(npm("css-loader", "6.7.3"))
                api(npm("style-loader", "3.3.2"))

                implementation(libs.kotlinxCoroutines)
            }
        }
    }
}

android {
    namespace = "dev.icerock.moko.resources"
}

dependencies {
    commonMainApi(libs.mokoParcelize)
    commonMainApi(libs.mokoGraphics)

    jvmMainImplementation(libs.icu4j)
    jvmMainImplementation(libs.batikRasterizer)
    jvmMainImplementation(libs.batikTranscoder)
    
    iosTestImplementation(libs.mokoTestCore)
}

tasks.named("publishToMavenLocal") {
    val pluginPublish = gradle.includedBuild("resources-generator")
        .task(":publishToMavenLocal")
    dependsOn(pluginPublish)
}

val copyIosX64TestResources = tasks.register<Copy>("copyIosX64TestResources") {
    from("src/iosTest/resources")
    into("build/bin/iosX64/debugTest")
}

tasks.findByName("iosX64Test")!!.dependsOn(copyIosX64TestResources)

val copyIosArm64TestResources = tasks.register<Copy>("copyIosArm64TestResources") {
    from("src/iosTest/resources")
    into("build/bin/iosSimulatorArm64/debugTest")
}

tasks.findByName("iosSimulatorArm64Test")!!.dependsOn(copyIosArm64TestResources)
