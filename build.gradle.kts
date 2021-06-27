/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

buildscript {
    repositories {
        mavenCentral()
        google()

        gradlePluginPortal()
    }
    dependencies {
        classpath("dev.icerock.moko:resources-generator")
        classpath(":resources-build-logic")
    }
}
