/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.factory

import dev.icerock.gradle.MRVisibility
import dev.icerock.gradle.generator.Constants
import dev.icerock.gradle.generator.FlatPropertiesGenerationStrategy
import dev.icerock.gradle.generator.PlatformResourceGenerator
import dev.icerock.gradle.generator.ResourceTypeGenerator
import dev.icerock.gradle.generator.resources.NOPResourceGenerator
import dev.icerock.gradle.generator.resources.image.AndroidImageResourceGenerator
import dev.icerock.gradle.generator.resources.image.AppleImageResourceGenerator
import dev.icerock.gradle.generator.resources.image.ImageResourceGenerator
import dev.icerock.gradle.generator.resources.image.JsImageResourceGenerator
import dev.icerock.gradle.generator.resources.image.WasmJsImageResourceGenerator
import dev.icerock.gradle.generator.resources.image.JvmImageResourceGenerator
import dev.icerock.gradle.metadata.container.ResourceType
import dev.icerock.gradle.metadata.resource.ImageMetadata
import dev.icerock.gradle.toModifier
import dev.icerock.gradle.utils.createByPlatform
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.slf4j.Logger
import java.io.File

@Suppress("LongParameterList")
internal class ImageGeneratorFactory(
    private val resourcesVisibility: MRVisibility,
    private val outputResourcesDir: File,
    private val outputAssetsDir: File,
    private val kotlinPlatformType: KotlinPlatformType,
    private val kotlinKonanTarget: () -> KonanTarget,
    private val androidRClassPackage: () -> String,
    private val logger: Logger
) {
    fun create(): ResourceTypeGenerator<ImageMetadata> {
        return ResourceTypeGenerator(
            propertiesGenerationStrategy = FlatPropertiesGenerationStrategy(),
            resourceClass = Constants.imageResourceName,
            resourceType = ResourceType.IMAGES,
            metadataClass = ImageMetadata::class,
            visibilityModifier = resourcesVisibility.toModifier(),
            generator = ImageResourceGenerator(),
            platformResourceGenerator = createPlatformImageGenerator(),
            filter = {
                include("images/**/*.png", "images/**/*.jpg", "images/**/*.svg")
            }
        )
    }

    private fun createPlatformImageGenerator(): PlatformResourceGenerator<ImageMetadata> {
        return createByPlatform(
            kotlinPlatformType = kotlinPlatformType,
            konanTarget = kotlinKonanTarget,
            // TODO find way to remove this NOP
            createCommon = { NOPResourceGenerator() },
            createAndroid = {
                AndroidImageResourceGenerator(
                    androidRClassPackage = androidRClassPackage(),
                    resourcesGenerationDir = outputResourcesDir,
                    logger = logger
                )
            },
            createApple = {
                AppleImageResourceGenerator(
                    assetsGenerationDir = outputAssetsDir
                )
            },
            createJvm = {
                JvmImageResourceGenerator(
                    resourcesGenerationDir = outputResourcesDir
                )
            },
            createJs = {
                JsImageResourceGenerator(
                    resourcesGenerationDir = outputResourcesDir
                )
            },
            createWasm = {
                WasmJsImageResourceGenerator(
                    resourcesGenerationDir = outputResourcesDir
                )
            }
        )
    }
}
