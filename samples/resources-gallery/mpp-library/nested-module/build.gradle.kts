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
    resourcesPackage.set("com.icerockdev.library.nested")
    resourcesClassName.set("NestedMR")
    resourcesVisibility.set(MRVisibility.Internal)
}
