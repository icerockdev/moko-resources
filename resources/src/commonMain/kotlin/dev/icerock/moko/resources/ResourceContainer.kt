/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

expect interface ResourceContainer<T>

expect fun ResourceContainer<ImageResource>.getImageByFileName(fileName: String): ImageResource?
expect fun ResourceContainer<AssetResource>.getAssetByFilePath(filePath: String): AssetResource

