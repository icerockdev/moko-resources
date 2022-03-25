/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

actual fun ResourceContainer<AssetResource>.getAssetByFilePath(filePath: String): AssetResource? {
    //get name without extension and extension
    val ext = filePath.substringAfterLast('.')
    val name = filePath.substringBeforeLast('.')
        .replace('/', '+')

    return AssetResource(filePath.removeFirstSlash(), name, ext, nsBundle)
}
