/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.utils

import org.apache.commons.codec.digest.DigestUtils
import java.io.File
import java.io.InputStream
import java.io.SequenceInputStream

internal fun File.calculateResourcesHash(): String {
    val inputStreams: List<InputStream> = walkTopDown()
        .filterNot { it.isDirectory }
        .map { it.inputStream() }.toList()
    val singleInputStream: InputStream = SequenceInputStream(inputStreams.toEnumeration())

    return singleInputStream.use { DigestUtils.md5Hex(it) }
}
