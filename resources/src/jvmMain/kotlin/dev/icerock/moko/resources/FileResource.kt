/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import java.io.FileNotFoundException

actual open class FileResource(
    val resourcesClassLoader: ClassLoader,
    val filePath: String
) {
    fun readText(): String {
        val stream = resourcesClassLoader.getResourceAsStream(filePath)
            ?: throw FileNotFoundException("Couldn't open resource as stream at: $filePath")
        return stream.use { it.readBytes().decodeToString() }
    }
}
