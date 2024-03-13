/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.utils

import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

internal val KotlinPlatformType.isCommon: Boolean
    get() = this == KotlinPlatformType.common
