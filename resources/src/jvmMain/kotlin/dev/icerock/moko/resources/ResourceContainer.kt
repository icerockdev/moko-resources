/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import java.io.File
import java.io.FileNotFoundException

actual interface ResourceContainer<T> {
    val resourcesClassLoader: ClassLoader
}

actual fun ResourceContainer<ImageResource>.getImageByFileName(
    fileName: String
): ImageResource? {
    return try {
        ImageResource(
            resourcesClassLoader = resourcesClassLoader,
            filePath = "images/$fileName"
        )
    } catch (exc: FileNotFoundException) {
        null
    }
}

actual fun ResourceContainer<AssetResource>.getAssetByFilePath(filePath: String): AssetResource {
    return AssetResource(resourcesClassLoader, "files${File.separatorChar}$filePath")
}
