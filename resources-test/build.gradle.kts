/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("multiplatform-library-convention")
    id("apple-main-convention")
    id("detekt-convention")
    id("javadoc-stub-convention")
    id("publication-convention")
}

group = "dev.icerock.moko"
version = libs.versions.mokoResourcesVersion.get()

dependencies {
    commonMainApi(project(":resources"))
}
