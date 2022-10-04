/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle

import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

open class MultiplatformResourcesPluginExtension {
    var multiplatformResourcesPackage: String? = null
    var multiplatformResourcesClassName: String = "MR"
    var multiplatformResourcesSourceSet: String? = null
    var iosBaseLocalizationRegion: String = "en"
    val sourceSetName: String
        get() = multiplatformResourcesSourceSet ?: KotlinSourceSet.COMMON_MAIN_SOURCE_SET_NAME
    var disableStaticFrameworkWarning = false
    var multiplatformResourcesVisibility: MRVisibility = MRVisibility.Public
    var addSuppressAnnotation: Boolean = false
}
