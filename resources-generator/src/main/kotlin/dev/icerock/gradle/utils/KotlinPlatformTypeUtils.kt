/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.utils

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget

internal val KotlinPlatformType.isCommon: Boolean
    get() = this == KotlinPlatformType.common

internal fun KotlinTarget.getPlatformType(): String {
    return if (this is KotlinMultiplatformAndroidLibraryTarget){
        KotlinPlatformType.androidJvm.name
    } else {
        platformType.name
    }
}