/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("org.jetbrains.kotlin.jvm") version("1.4.21")
}

repositories {
    mavenLocal()
    jcenter()
    google()

    maven { url = uri("https://dl.bintray.com/icerockdev/plugins") }
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation("dev.icerock:mobile-multiplatform:0.9.0")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.21")
    implementation("com.android.tools.build:gradle:4.0.1")
    implementation("org.jetbrains.compose:compose-gradle-plugin:0.3.0-build140")
}
