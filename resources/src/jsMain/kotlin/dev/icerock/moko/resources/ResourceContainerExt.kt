/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

actual fun ResourceContainer<AssetResource>.getAssetByFilePath(
    filePath: String
): AssetResource? = values()
    .find { it.rawPath == filePath }
