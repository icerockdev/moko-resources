/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.ClassName
import dev.icerock.gradle.generator.Constants.PlatformDetails.platformDetailsPropertyName

internal object Constants {
    val resourcePlatformDetailsName = ClassName("dev.icerock.moko.resources", "ResourcePlatformDetails")
    val resourceContainerName = ClassName("dev.icerock.moko.resources", "ResourceContainer")
    val stringResourceName = ClassName("dev.icerock.moko.resources", "StringResource")
    val pluralsResourceName = ClassName("dev.icerock.moko.resources", "PluralsResource")
    val imageResourceName = ClassName("dev.icerock.moko.resources", "ImageResource")
    val colorResourceName = ClassName("dev.icerock.moko.resources", "ColorResource")
    val fontResourceName = ClassName("dev.icerock.moko.resources", "FontResource")
    val fileResourceName = ClassName("dev.icerock.moko.resources", "FileResource")
    val assetResourceName = ClassName("dev.icerock.moko.resources", "AssetResource")

    val graphicsColorName = ClassName("dev.icerock.moko.graphics", "Color")

    object PlatformDetails {
        const val platformDetailsPropertyName = "__platformDetails"
        const val platformDetailsClass: String = "ResourcePlatformDetails"
    }

    object Apple {
        val nsBundleName = ClassName("platform.Foundation", "NSBundle")
        val loadableBundleName = ClassName("dev.icerock.moko.resources.utils", "loadableBundle")

        const val assetsDirectoryName = "Assets.xcassets"

        const val resourcesBundlePropertyName = "bundle"
        private const val containerBundlePropertyName = "nsBundle"
        val platformContainerBundlePropertyName = "$platformDetailsPropertyName.$containerBundlePropertyName"
    }

    object Jvm {
        val classLoaderName = ClassName("java.lang", "ClassLoader")
        const val resourcesClassLoaderPropertyName = "resourcesClassLoader"
        const val localizationDir = "localization"
    }

    object Js {
        private const val internalPackage = "dev.icerock.moko.resources.internal"
        val supportedLocalesName = ClassName(internalPackage, "SupportedLocales")
        val supportedLocaleName = ClassName(internalPackage, "SupportedLocale")

        const val remoteStringLoaderClassName = "RemoteJsStringLoader"
        val loaderHolderName = ClassName(internalPackage, "${remoteStringLoaderClassName}Holder")
        val stringLoaderName =
            ClassName("dev.icerock.moko.resources.provider", remoteStringLoaderClassName)
        const val stringsLoaderPropertyName = "stringsLoader"

        const val fallbackFilePropertyName = "fallbackFileUrl"
        const val supportedLocalesPropertyName = "supportedLocales"
    }
}
