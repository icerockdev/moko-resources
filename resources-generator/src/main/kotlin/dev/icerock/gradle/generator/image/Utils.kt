/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.image

import dev.icerock.gradle.metadata.resource.ImageMetadata
import java.io.File

internal fun generateHighestQualityImageResources(
    resourcesGenerationDir: File,
    data: List<ImageMetadata>,
    imagesDirName: String
) {
    val imagesDir = File(resourcesGenerationDir, imagesDirName)
    imagesDir.mkdirs()

    data.forEach { metadata ->
        val item: ImageMetadata.ImageQualityItem = metadata.getHighestQualityItem()
        val file: File = item.filePath
        val key: String = metadata.key

        file.copyTo(File(imagesDir, "$key.${file.extension}"))
    }
}

internal fun ImageMetadata.getHighestQualityItem(): ImageMetadata.ImageQualityItem {
    return values.singleOrNull { it.quality == null }
        ?: values.maxBy { it.quality!!.toDouble() }
}
