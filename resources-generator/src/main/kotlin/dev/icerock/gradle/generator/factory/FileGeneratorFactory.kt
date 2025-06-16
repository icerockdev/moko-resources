/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.factory

import dev.icerock.gradle.MRVisibility
import dev.icerock.gradle.generator.Constants
import dev.icerock.gradle.generator.HierarchyPropertiesGenerationStrategy
import dev.icerock.gradle.generator.PlatformResourceGenerator
import dev.icerock.gradle.generator.ResourceTypeGenerator
import dev.icerock.gradle.generator.platform.js.JsFilePathMode
import dev.icerock.gradle.generator.resources.NOPResourceGenerator
import dev.icerock.gradle.generator.resources.file.AndroidFileResourceGenerator
import dev.icerock.gradle.generator.resources.file.AppleFileResourceGenerator
import dev.icerock.gradle.generator.resources.file.FileResourceGenerator
import dev.icerock.gradle.generator.resources.file.JsFileResourceGenerator
import dev.icerock.gradle.generator.resources.file.JvmFileResourceGenerator
import dev.icerock.gradle.metadata.container.ResourceType
import dev.icerock.gradle.metadata.resource.FileMetadata
import dev.icerock.gradle.toModifier
import dev.icerock.gradle.utils.createByPlatform
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.internal.file.collections.FileCollectionAdapter
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File

@Suppress("LongParameterList")
internal class FileGeneratorFactory(
    private val resourcesVisibility: MRVisibility,
    private val outputResourcesDir: File,
    private val kotlinPlatformType: KotlinPlatformType,
    private val kotlinKonanTarget: () -> KonanTarget,
    private val androidRClassPackage: () -> String,
    private val ownResources: ConfigurableFileCollection
) {
    fun create(): ResourceTypeGenerator<FileMetadata> {
        return ResourceTypeGenerator(
            propertiesGenerationStrategy = HierarchyPropertiesGenerationStrategy(),
            resourceClass = Constants.fileResourceName,
            resourceType = ResourceType.FILES,
            metadataClass = FileMetadata::class,
            visibilityModifier = resourcesVisibility.toModifier(),
            generator = FileResourceGenerator(
                fileDirs = ownResources.from
                    .map { it as FileCollectionAdapter }
                    .flatMap { it.files }
                    .map { File(it, "files") }
                    .toSet()
            ),
            platformResourceGenerator = createPlatformFileGenerator(),
            filter = { include("files/**") }
        )
    }

    private fun createPlatformFileGenerator(): PlatformResourceGenerator<FileMetadata> {
        return createByPlatform(
            kotlinPlatformType = kotlinPlatformType,
            konanTarget = kotlinKonanTarget,
            // TODO find way to remove this NOP
            createCommon = { NOPResourceGenerator() },
            createAndroid = {
                AndroidFileResourceGenerator(
                    androidRClassPackage = androidRClassPackage(),
                    resourcesGenerationDir = outputResourcesDir
                )
            },
            createApple = {
                AppleFileResourceGenerator(
                    resourcesGenerationDir = outputResourcesDir
                )
            },
            createJvm = {
                JvmFileResourceGenerator(
                    resourcesGenerationDir = outputResourcesDir
                )
            },
            createJs = {
                JsFileResourceGenerator(
                    resourcesGenerationDir = outputResourcesDir,
                    filePathMode = JsFilePathMode.require
                )
            },
            createWasm = {
                JsFileResourceGenerator(
                    resourcesGenerationDir = outputResourcesDir,
                    filePathMode = JsFilePathMode.rawPath
                )
            }
        )
    }
}
