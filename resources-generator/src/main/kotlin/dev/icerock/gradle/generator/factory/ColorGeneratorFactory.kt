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
import dev.icerock.gradle.generator.resources.color.AndroidColorResourceGenerator
import dev.icerock.gradle.generator.resources.color.AppleColorResourceGenerator
import dev.icerock.gradle.generator.resources.color.ColorResourceGenerator
import dev.icerock.gradle.generator.resources.color.JsColorResourceGenerator
import dev.icerock.gradle.generator.resources.color.JvmColorResourceGenerator
import dev.icerock.gradle.metadata.container.ResourceType
import dev.icerock.gradle.metadata.resource.ColorMetadata
import dev.icerock.gradle.toModifier
import dev.icerock.gradle.utils.createByPlatform
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File

@Suppress("LongParameterList")
internal class ColorGeneratorFactory(
    private val resourcesVisibility: MRVisibility,
    private val outputResourcesDir: File,
    private val outputAssetsDir: File,
    private val kotlinPlatformType: KotlinPlatformType,
    private val kotlinKonanTarget: () -> KonanTarget,
    private val androidRClassPackage: () -> String,
) {
    fun create(): ResourceTypeGenerator<ColorMetadata> {
        return ResourceTypeGenerator(
            propertiesGenerationStrategy = FlatPropertiesGenerationStrategy(),
            resourceClass = Constants.colorResourceName,
            resourceType = ResourceType.COLORS,
            metadataClass = ColorMetadata::class,
            visibilityModifier = resourcesVisibility.toModifier(),
            generator = ColorResourceGenerator(),
            platformResourceGenerator = createPlatformColorGenerator(),
            filter = { include("**/colors*.xml") }
        )
    }

    private fun createPlatformColorGenerator(): PlatformResourceGenerator<ColorMetadata> {
        return createByPlatform(
            kotlinPlatformType = kotlinPlatformType,
            konanTarget = kotlinKonanTarget,
            // TODO find way to remove this NOP
            createCommon = { NOPResourceGenerator() },
            createAndroid = {
                AndroidColorResourceGenerator(
                    androidRClassPackage = androidRClassPackage(),
                    resourcesGenerationDir = outputResourcesDir
                )
            },
            createApple = {
                AppleColorResourceGenerator(
                    assetsGenerationDir = outputAssetsDir
                )
            },
            createJvm = {
                JvmColorResourceGenerator()
            },
            createJs = {
                JsColorResourceGenerator()
            }
        )
    }
}
