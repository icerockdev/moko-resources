/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.rework

import com.squareup.kotlinpoet.ClassName

object CodeConst {
    val resourceContainerClass = ClassName("dev.icerock.moko.resources", "ResourceContainer")

    object Apple {
        val nsBundleClass = ClassName("platform.Foundation", "NSBundle")
        val loadableBundleClass = ClassName("dev.icerock.moko.resources.utils", "loadableBundle")
        const val resourcesBundlePropertyName = "bundle"
        const val containerBundlePropertyName = "nsBundle"
    }

    object Jvm {
        val classLoaderClass = ClassName("java.lang", "ClassLoader")
        const val resourcesClassLoaderPropertyName = "resourcesClassLoader"
        const val localizationDir = "localization"
    }
}