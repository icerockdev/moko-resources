/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
        gradlePluginPortal()
    }
    dependencies {
        classpath(moko.resourcesGradlePlugin)
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.10")
        classpath("com.android.tools.build:gradle:8.11.1")

    }
}
