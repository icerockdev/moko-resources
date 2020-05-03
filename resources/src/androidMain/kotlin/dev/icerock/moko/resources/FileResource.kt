/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import java.io.File
import java.net.URI

actual class FileResource(
    val assetsPath: String
) {
    actual fun readText(): String {
        val assetUri: URI = URI.create("file:///android_asset/$assetsPath")
        val file: File = File(assetUri)
        return file.readText()
    }
}
