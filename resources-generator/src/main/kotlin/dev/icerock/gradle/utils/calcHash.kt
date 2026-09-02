/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("Filename")

package dev.icerock.gradle.utils

import org.apache.commons.codec.digest.DigestUtils
import java.io.File
import java.io.InputStream
import java.io.SequenceInputStream

internal fun File.calculateResourcesHash(): String {
    val root: File = this
    // Sort for deterministic output; walkTopDown() order is filesystem-dependent.
    val inputStreams: List<InputStream> = walkTopDown()
        .filterNot { it.isDirectory }
        .sortedBy { it.relativeTo(root).path }
        .map { it.inputStream() }.toList()
    val singleInputStream: InputStream = SequenceInputStream(inputStreams.toEnumeration())

    return singleInputStream.use { DigestUtils.md5Hex(it) }
}

internal fun List<String>.calculateHash(): String {
    return DigestUtils.md5Hex(this.joinToString(":"))
}
