/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.factory

import dev.icerock.gradle.MRVisibility
import dev.icerock.gradle.generator.Constants
import dev.icerock.gradle.generator.PlatformResourceGenerator
import dev.icerock.gradle.generator.ResourceTypeGenerator
import dev.icerock.gradle.generator.resources.NOPResourceGenerator
import dev.icerock.gradle.generator.resources.asset.AndroidAssetResourceGenerator
import dev.icerock.gradle.generator.resources.asset.AppleAssetResourceGenerator
import dev.icerock.gradle.generator.resources.asset.AssetResourceGenerator
import dev.icerock.gradle.generator.resources.asset.JsAssetResourceGenerator
import dev.icerock.gradle.generator.resources.asset.JvmAssetResourceGenerator
import dev.icerock.gradle.metadata.container.ResourceType
import dev.icerock.gradle.metadata.resource.AssetMetadata
import dev.icerock.gradle.toModifier
import dev.icerock.gradle.utils.createByPlatform
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.internal.file.collections.FileCollectionAdapter
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File

@Suppress("LongParameterList")
internal class AssetGeneratorFactory(
    private val resourcesPackageName: String,
    private val resourcesVisibility: MRVisibility,
    private val outputResourcesDir: File,
    private val outputAssetsDir: File,
    private val kotlinPlatformType: KotlinPlatformType,
    private val kotlinKonanTarget: () -> KonanTarget,
    private val androidRClassPackage: () -> String,
    private val ownResources: ConfigurableFileCollection
) {
    fun create(): ResourceTypeGenerator<AssetMetadata> {
        return ResourceTypeGenerator(
            generationPackage = resourcesPackageName,
            resourceClass = Constants.assetResourceName,
            resourceType = ResourceType.ASSETS,
            metadataClass = AssetMetadata::class,
            visibilityModifier = resourcesVisibility.toModifier(),
            generator = AssetResourceGenerator(
                assetDirs = ownResources.from
                    .map { it as FileCollectionAdapter }
                    .flatMap { it.files }
                    .map { File(it, "assets") }
                    .toSet()
            ),
            platformResourceGenerator = createPlatformAssetGenerator(),
            filter = { include("assets/**") }
        )
    }

    private fun createPlatformAssetGenerator(): PlatformResourceGenerator<AssetMetadata> {
        return createByPlatform(
            kotlinPlatformType = kotlinPlatformType,
            konanTarget = kotlinKonanTarget,
            // TODO find way to remove this NOP
            createCommon = { NOPResourceGenerator() },
            createAndroid = {
                AndroidAssetResourceGenerator(
                    androidRClassPackage = androidRClassPackage(),
                    assetsGenerationDir = outputAssetsDir,
                )
            },
            createApple = {
                AppleAssetResourceGenerator(
                    resourcesGenerationDir = outputResourcesDir
                )
            },
            createJvm = {
                JvmAssetResourceGenerator(
                    resourcesGenerationDir = outputResourcesDir
                )
            },
            createJs = {
                JsAssetResourceGenerator(
                    resourcesGenerationDir = outputResourcesDir
                )
            }
        )
    }
}
