/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.utils

import java.io.File

internal val File.svg: Boolean get() =
    extension.equals("svg", ignoreCase = true)

internal val File.scale: String get() =
    nameWithoutExtension.substringAfter("@")

internal val File.nameWithoutScale: String get() =
    nameWithoutExtension.withoutScale
