/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import java.io.FileNotFoundException

actual class FileResource(private val path: String) {

    fun readText(): String = with(Thread.currentThread().contextClassLoader) {
        getResourceAsStream(path)?.readBytes()?.decodeToString()
            ?: throw FileNotFoundException("Couldn't open resource as stream at: $path")
    }
}