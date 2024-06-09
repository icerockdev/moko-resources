/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

actual data class ImageResource(
    val fileName: String,
    val fileUrl: String,
    val darkFileUrl: String?,
) {
    constructor(
        fileName: String,
        fileUrl: String,
    ) : this(
        fileName = fileName,
        fileUrl = fileUrl,
        darkFileUrl = null
    )
}
