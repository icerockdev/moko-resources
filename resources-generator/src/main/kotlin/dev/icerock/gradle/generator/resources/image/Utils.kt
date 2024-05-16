/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.resources.image

import dev.icerock.gradle.metadata.resource.ImageMetadata
import dev.icerock.gradle.metadata.resource.ImageMetadata.Appearance.DARK
import dev.icerock.gradle.metadata.resource.ImageMetadata.ImageItem
import java.io.File

internal fun generateHighestQualityImageResources(
    resourcesGenerationDir: File,
    data: List<ImageMetadata>,
    imagesDirName: String,
) {
    val imagesDir = File(resourcesGenerationDir, imagesDirName)
    imagesDir.mkdirs()

    data.forEach { metadata ->
        metadata.values
            .groupBy { it.appearance }
            .forEach { (theme, list: List<ImageItem>) ->
                val item: ImageMetadata.ImageItem = list.getHighestQualityItem(theme)
                val file: File = item.filePath
                val key: String = metadata.key

                val fileName: String = if (theme == DARK) {
                    "$key${theme.themeSuffix}.${file.extension}"
                } else {
                    "$key.${file.extension}"
                }

                file.copyTo(File(imagesDir, fileName))
            }
    }
}

internal fun List<ImageItem>.getHighestQualityItem(
    appearance: ImageMetadata.Appearance,
): ImageMetadata.ImageItem {
    val filteredList = filter { it.appearance == appearance }

    return filteredList.singleOrNull { it.quality == null }
        ?: filteredList.maxBy { it.quality!!.toDouble() }
}
