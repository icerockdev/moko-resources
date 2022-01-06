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

    js {
        nodejs()
        browser()
        useCommonJs()
    }

    sourceSets {
        getByName("jsMain") {
            dependencies {
                api(npm("bcp-47", "1.0.8"))
                api(npm("sprintf-js", "1.1.2"))

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.6.0")
            }
        }
    }
}

dependencies {
    commonMainApi(libs.mokoParcelize)
    commonMainApi(libs.mokoGraphics)

    jvmMainImplementation(libs.icu4j)

    androidMainImplementation(libs.appCompat)

    iosTestImplementation(libs.mokoTestCore)
}

tasks.named("publishToMavenLocal") {
    val pluginPublish = gradle.includedBuild("resources-generator")
        .task(":publishToMavenLocal")
    dependsOn(pluginPublish)
}

val copyIosTestResources = tasks.register<Copy>("copyIosTestResources") {
    from("src/iosTest/resources")
    into("build/bin/iosX64/debugTest")
}

tasks.findByName("iosX64Test")!!.dependsOn(copyIosTestResources)
