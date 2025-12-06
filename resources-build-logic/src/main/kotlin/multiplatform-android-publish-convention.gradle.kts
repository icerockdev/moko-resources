/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

kotlin {
    androidTarget {
        publishLibraryVariants()
        publishLibraryVariantsGroupedByFlavor = true
    }
}
