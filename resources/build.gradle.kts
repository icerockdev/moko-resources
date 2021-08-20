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

group = "dev.icerock.moko"
version = libs.versions.mokoResourcesVersion.get()

kotlin {
    sourceSets {
        val commonMain by getting
        val jvmMain by getting
    }
    targets
        .matching { it is org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget }
        .configureEach {
            this as org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

            compilations.getByName("main") {
                val pluralizedString by cinterops.creating {
                    defFile(project.file("src/appleMain/def/pluralizedString.def"))
                }
            }
        }
}

dependencies {
    jvmImplementation(libs.icu4j)
    commonMainApi(libs.mokoParcelize)
    commonMainApi(libs.mokoGraphics)

    "androidMainImplementation"(libs.appCompat)
}

tasks.named("publishToMavenLocal") {
    val pluginPublish = gradle.includedBuild("resources-generator")
        .task(":publishToMavenLocal")
    dependsOn(pluginPublish)
}
