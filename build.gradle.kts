/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

buildscript {
    repositories {
        mavenCentral()
        google()

        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")

        gradlePluginPortal()
    }
    dependencies {
        classpath("dev.icerock.moko:resources-generator")
        classpath("org.jetbrains.compose:compose-gradle-plugin:0.5.0-build270")
        classpath(":resources-build-logic")
    }
}
