/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.utils

import dev.icerock.gradle.metadata.resource.Appearance
import java.io.File
import org.jetbrains.kotlin.konan.file.File as KonanFile

internal val File.svg: Boolean
    get() =
        extension.equals("svg", ignoreCase = true)

internal val File.scale: String
    get() =
        nameWithoutExtension.substringAfter("@").substringBefore("x")

internal val File.nameWithoutScale: String
    get() =
        nameWithoutExtension.withoutScale

internal val File.appearance: Appearance?
    get() =
        nameWithoutExtension.appearance

internal fun File.toKonanFile(): KonanFile = KonanFile(this.path)
