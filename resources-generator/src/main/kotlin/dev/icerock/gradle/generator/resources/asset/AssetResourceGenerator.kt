/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.resources.asset

import com.squareup.kotlinpoet.PropertySpec
import dev.icerock.gradle.generator.Constants
import dev.icerock.gradle.generator.ResourceGenerator
import dev.icerock.gradle.generator.generateKey
import dev.icerock.gradle.metadata.resource.AssetMetadata
import java.io.File

internal class AssetResourceGenerator(
    private val assetDirs: Set<File>
) : ResourceGenerator<AssetMetadata> {

    override fun generateMetadata(files: Set<File>): List<AssetMetadata> {
        return files.map { file ->
            AssetMetadata(
                key = generateKey(file.nameWithoutExtension),
                relativePath = assetDirs.single { file.absolutePath.contains(it.absolutePath) },
                filePath = file,
            )
        }
    }

    override fun generateProperty(metadata: AssetMetadata): PropertySpec.Builder {
        return PropertySpec.builder(metadata.key, Constants.assetResourceName)
    }
}
