/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.resources.file

import com.squareup.kotlinpoet.PropertySpec
import dev.icerock.gradle.generator.Constants
import dev.icerock.gradle.generator.ResourceGenerator
import dev.icerock.gradle.generator.generateKey
import dev.icerock.gradle.metadata.resource.FileMetadata
import java.io.File

internal class FileResourceGenerator : ResourceGenerator<FileMetadata> {

    override fun generateMetadata(files: Set<File>): List<FileMetadata> {
        return files.map { file ->
            FileMetadata(
                key = generateKey(file.name),
                filePath = file,
            )
        }
    }

    override fun generateProperty(metadata: FileMetadata): PropertySpec.Builder {
        return PropertySpec.builder(metadata.key, Constants.fileResourceName)
    }
}
