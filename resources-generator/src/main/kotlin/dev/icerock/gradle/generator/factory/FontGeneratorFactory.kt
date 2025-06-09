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
import dev.icerock.gradle.generator.resources.font.AndroidFontResourceGenerator
import dev.icerock.gradle.generator.resources.font.AppleFontResourceGenerator
import dev.icerock.gradle.generator.resources.font.FontResourceGenerator
import dev.icerock.gradle.generator.resources.font.JsFontResourceGenerator
import dev.icerock.gradle.generator.resources.font.JvmFontResourceGenerator
import dev.icerock.gradle.generator.resources.font.WasmJsFontResourceGenerator
import dev.icerock.gradle.metadata.container.ResourceType
import dev.icerock.gradle.metadata.resource.FontMetadata
import dev.icerock.gradle.toModifier
import dev.icerock.gradle.utils.createByPlatform
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File

@Suppress("LongParameterList")
internal class FontGeneratorFactory(
    private val resourcesPackageName: String,
    private val resourcesVisibility: MRVisibility,
    private val outputResourcesDir: File,
    private val kotlinPlatformType: KotlinPlatformType,
    private val kotlinKonanTarget: () -> KonanTarget,
    private val androidRClassPackage: () -> String,
) {
    fun create(): ResourceTypeGenerator<FontMetadata> {
        return ResourceTypeGenerator(
            propertiesGenerationStrategy = FlatPropertiesGenerationStrategy(),
            resourceClass = Constants.fontResourceName,
            resourceType = ResourceType.FONTS,
            metadataClass = FontMetadata::class,
            visibilityModifier = resourcesVisibility.toModifier(),
            generator = FontResourceGenerator(),
            platformResourceGenerator = createPlatformFontGenerator(),
            filter = { include("fonts/**.ttf", "fonts/**.otf") }
        )
    }

    private fun createPlatformFontGenerator(): PlatformResourceGenerator<FontMetadata> {
        return createByPlatform(
            kotlinPlatformType = kotlinPlatformType,
            konanTarget = kotlinKonanTarget,
            // TODO find way to remove this NOP
            createCommon = { NOPResourceGenerator() },
            createAndroid = {
                AndroidFontResourceGenerator(
                    androidRClassPackage = androidRClassPackage(),
                    resourcesGenerationDir = outputResourcesDir
                )
            },
            createApple = {
                AppleFontResourceGenerator(
                    resourcesGenerationDir = outputResourcesDir
                )
            },
            createJvm = {
                JvmFontResourceGenerator(
                    resourcesGenerationDir = outputResourcesDir
                )
            },
            createJs = {
                JsFontResourceGenerator(
                    resourcesPackageName = resourcesPackageName,
                    resourcesGenerationDir = outputResourcesDir
                )
            },
            createWasm = {
                WasmJsFontResourceGenerator(
                    resourcesPackageName = resourcesPackageName,
                    resourcesGenerationDir = outputResourcesDir
                )
            }
        )
    }
}
