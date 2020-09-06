/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    plugin(Deps.Plugins.javaGradle)
    plugin(Deps.Plugins.kotlinJvm)
    plugin(Deps.Plugins.mavenPublish)
    plugin(Deps.Plugins.detekt)
}

repositories {
    jcenter()
    google()
}

group = "dev.icerock.moko"
version = Deps.mokoResourcesVersion

dependencies {
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
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}
