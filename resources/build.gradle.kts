/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("dev.icerock.mobile.multiplatform")
    id("kotlin-parcelize")
    id("org.gradle.maven-publish")
}

group = "dev.icerock.moko"
version = Deps.mokoResourcesVersion

dependencies {
    commonMainApi(Deps.Libs.MultiPlatform.mokoParcelize)
    commonMainApi(Deps.Libs.MultiPlatform.mokoGraphics.common)

    androidMainImplementation(Deps.Libs.Android.appCompat)
}

publishing {
    repositories.maven("https://api.bintray.com/maven/icerockdev/moko/moko-resources/;publish=1") {
        name = "bintray"

        credentials {
            username = System.getProperty("BINTRAY_USER")
            password = System.getProperty("BINTRAY_KEY")
        }
    }
}

kotlin {
    macosX64()
    sourceSets {
        val commonMain by getting {}

        val appleMain by creating {
            dependsOn(commonMain)
        }
        val iosMain by getting {
            dependsOn(appleMain)
        }
        val macosX64Main by getting {
            dependsOn(appleMain)
        }
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

tasks.named("publishToMavenLocal") {
    val pluginPublish = gradle.includedBuild("plugins")
        .task(":resources-generator:publishToMavenLocal")
    dependsOn(pluginPublish)
}
