/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import dev.icerock.moko.resources.internal.RemoteJsStringLoaderHolder

actual interface ResourceContainer<T>

actual fun ResourceContainer<ImageResource>.getImageByFileName(
    fileName: String
): ImageResource? {
    TODO("Not yet implemented")
}

actual fun ResourceContainer<AssetResource>.getAssetByFilePath(
    filePath: String
): AssetResource {
    TODO("Not yet implemented")
}
