/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("multiplatform-library-convention")
    id("dev.icerock.mobile.multiplatform.apple-framework")
    id("dev.icerock.mobile.multiplatform-resources")
    id("detekt-convention")
}

android {
    lintOptions {
        disable("ImpliedQuantity")
    }
}

dependencies {
    commonMainApi(projects.resources)
    commonMainApi(libs.mokoGraphics)
    commonMainImplementation(projects.sample.mppLibrary.nestedModule)

    commonTestImplementation(libs.kotlinTest)
    commonTestImplementation(libs.kotlinTestAnnotations)
    commonTestImplementation(projects.resourcesTest)
 
    "androidTestImplementation"(libs.kotlinTestJUnit)
    "androidTestImplementation"(libs.testCore)
}

multiplatformResources {
    multiplatformResourcesPackage = "com.icerockdev.library"
}

framework {
    export(projects.resources)
    export(libs.mokoGraphics)
}
