/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    plugin(Deps.Plugins.androidLibrary)
    plugin(Deps.Plugins.kotlinMultiplatform)
    plugin(Deps.Plugins.mobileMultiplatform)
    plugin(Deps.Plugins.appleFramework)
    plugin(Deps.Plugins.mokoResources)
}

android {
    lintOptions {
        disable("ImpliedQuantity")
    }
}

kotlin {
    macosX64()
}

dependencies {
    commonMainApi(Deps.Libs.MultiPlatform.mokoResources)
    commonMainApi(Deps.Libs.MultiPlatform.mokoGraphics.common)

// disabled while not fixed https://youtrack.jetbrains.com/issue/KT-41384
//    commonMainImplementation(project("$path:nested-module"))
}

multiplatformResources {
    multiplatformResourcesPackage = "com.icerockdev.library"
}

framework {
    export(Deps.Libs.MultiPlatform.mokoGraphics)
}
