/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.resources.font

import com.squareup.kotlinpoet.PropertySpec
import dev.icerock.gradle.generator.Constants
import dev.icerock.gradle.generator.ResourceGenerator
import dev.icerock.gradle.generator.generateKey
import dev.icerock.gradle.metadata.resource.FontMetadata
import java.io.File

internal class FontResourceGenerator : ResourceGenerator<FontMetadata> {

    override fun generateMetadata(files: Set<File>): List<FontMetadata> {
        return files.map { file ->
            val key: String = file.nameWithoutExtension
                .replace('-', '_')
                .lowercase()

            FontMetadata(
                key = generateKey(key),
                filePath = file,
            )
        }
    }

    override fun generateProperty(metadata: FontMetadata): PropertySpec.Builder {
        return PropertySpec.builder(metadata.key, Constants.fontResourceName)
    }
}
