/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("dev.icerock.mobile.multiplatform.android-manifest")
    id("dev.icerock.mobile.multiplatform-resources")
}

kotlin {
    android()
    ios()
    macosX64()
}

dependencies {
    commonMainApi(projects.resources)
}

multiplatformResources {
    multiplatformResourcesPackage = "com.icerockdev.library.nested"
}
