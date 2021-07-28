/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
id("multiplatform-library-convention")
    id("dev.icerock.mobile.multiplatform-resources")
    id("detekt-convention")
}

kotlin {
    android()
    ios()
    macosX64()
    jvm()
}

dependencies {
    commonMainApi(projects.resources)
}

multiplatformResources {
    multiplatformResourcesPackage = "com.icerockdev.library.nested"
}
