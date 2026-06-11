/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

expect fun ResourceContainer<ImageResource>.getImageByFileName(fileName: String): ImageResource?
expect fun ResourceContainer<AssetResource>.getAssetByFilePath(filePath: String): AssetResource?

interface ResourceContainer<T> {
    @Suppress("VariableNaming")
    @Deprecated(
        message = "Internal resource container detail",
        level = DeprecationLevel.HIDDEN
    )
    val __platformDetails: ResourcePlatformDetails
    fun values(): List<T>
}
