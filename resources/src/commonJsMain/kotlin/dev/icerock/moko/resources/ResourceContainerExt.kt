/*
 * Copyright 2025 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

actual fun ResourceContainer<ImageResource>.getImageByFileName(
    fileName: String
): ImageResource? = values()
    .find { it.fileName == fileName }
