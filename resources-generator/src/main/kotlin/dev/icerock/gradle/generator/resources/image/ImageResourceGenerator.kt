/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.resources.image

import com.squareup.kotlinpoet.PropertySpec
import dev.icerock.gradle.generator.Constants
import dev.icerock.gradle.generator.ResourceGenerator
import dev.icerock.gradle.generator.generateKey
import dev.icerock.gradle.metadata.resource.ImageMetadata
import dev.icerock.gradle.utils.nameWithoutScale
import dev.icerock.gradle.utils.scale
import dev.icerock.gradle.utils.svg
import java.io.File

internal class ImageResourceGenerator : ResourceGenerator<ImageMetadata> {

    override fun generateMetadata(files: Set<File>): List<ImageMetadata> {
        return files.groupBy { extractKey(it) }.map { (key: String, files: List<File>) ->
            ImageMetadata(
                key = generateKey(key),
                values = files.map { file ->
                    ImageMetadata.ImageQualityItem(
                        quality = if (file.svg) null else file.scale,
                        filePath = file
                    )
                }
            )
        }
    }

    override fun generateProperty(metadata: ImageMetadata): PropertySpec.Builder {
        return PropertySpec.builder(metadata.key, Constants.imageResourceName)
    }

    private fun extractKey(file: File): String {
        return if (file.svg) {
            file.nameWithoutExtension
        } else {
            file.nameWithoutScale
        }
    }
}
