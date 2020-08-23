/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("dev.icerock.mobile.multiplatform")
    id("dev.icerock.mobile.multiplatform-resources")
}

dependencies {
    commonMainImplementation(Deps.Libs.MultiPlatform.mokoResources)
}

multiplatformResources {
    multiplatformResourcesPackage = "com.icerockdev.library.nested"
}
