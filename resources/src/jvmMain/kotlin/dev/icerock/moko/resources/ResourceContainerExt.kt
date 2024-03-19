/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import java.io.FileNotFoundException

actual fun ResourceContainer<ImageResource>.getImageByFileName(
    fileName: String
): ImageResource? {
    return try {
        ImageResource(
            resourcesClassLoader = __platformDetails.resourcesClassLoader,
            filePath = "images/$fileName"
        )
    } catch (exc: FileNotFoundException) {
        null
    }
}

actual fun ResourceContainer<AssetResource>.getAssetByFilePath(filePath: String): AssetResource? {
    val originalPath: String = filePath.removeFirstSlash()

    return AssetResource(
        resourcesClassLoader = __platformDetails.resourcesClassLoader,
        originalPath = originalPath,
        path = "assets/$originalPath"
    )
}
