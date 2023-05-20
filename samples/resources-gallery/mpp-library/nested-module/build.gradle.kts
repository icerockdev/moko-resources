/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import dev.icerock.gradle.MRVisibility

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("dev.icerock.mobile.multiplatform-resources")
}

android {
    namespace = "com.icerockdev.library.nested"
}

dependencies {
    commonMainApi(moko.resources)
}

multiplatformResources {
    multiplatformResourcesPackage = "com.icerockdev.library.nested"
    multiplatformResourcesClassName = "NestedMR"
    multiplatformResourcesVisibility = MRVisibility.Internal
}
