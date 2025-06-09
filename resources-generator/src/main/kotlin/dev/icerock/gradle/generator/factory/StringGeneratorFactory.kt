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
import dev.icerock.gradle.generator.resources.string.AndroidStringResourceGenerator
import dev.icerock.gradle.generator.resources.string.AppleStringResourceGenerator
import dev.icerock.gradle.generator.resources.string.JsStringResourceGenerator
import dev.icerock.gradle.generator.resources.string.JvmStringResourceGenerator
import dev.icerock.gradle.generator.resources.string.StringResourceGenerator
import dev.icerock.gradle.generator.resources.string.WasmJsStringResourceGenerator
import dev.icerock.gradle.metadata.container.ResourceType
import dev.icerock.gradle.metadata.resource.StringMetadata
import dev.icerock.gradle.toModifier
import dev.icerock.gradle.utils.createByPlatform
import dev.icerock.gradle.utils.flatName
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File

@Suppress("LongParameterList")
internal class StringGeneratorFactory(
    private val resourcesPackageName: String,
    private val resourcesVisibility: MRVisibility,
    private val strictLineBreaks: Boolean,
    private val outputResourcesDir: File,
    private val kotlinPlatformType: KotlinPlatformType,
    private val kotlinKonanTarget: () -> KonanTarget,
    private val androidRClassPackage: () -> String,
    private val iosBaseLocalizationRegion: () -> String
) {
    fun create(): ResourceTypeGenerator<StringMetadata> {
        return ResourceTypeGenerator(
            propertiesGenerationStrategy = FlatPropertiesGenerationStrategy(),
            resourceClass = Constants.stringResourceName,
            resourceType = ResourceType.STRINGS,
            metadataClass = StringMetadata::class,
            visibilityModifier = resourcesVisibility.toModifier(),
            generator = StringResourceGenerator(
                strictLineBreaks = strictLineBreaks
            ),
            platformResourceGenerator = createPlatformStringGenerator(),
            filter = { include("**/strings*.xml") }
        )
    }

    private fun createPlatformStringGenerator(): PlatformResourceGenerator<StringMetadata> {
        return createByPlatform(
            kotlinPlatformType = kotlinPlatformType,
            konanTarget = kotlinKonanTarget,
            // TODO find way to remove this NOP
            createCommon = { NOPResourceGenerator() },
            createAndroid = {
                AndroidStringResourceGenerator(
                    androidRClassPackage = androidRClassPackage(),
                    resourcesGenerationDir = outputResourcesDir
                )
            },
            createApple = {
                AppleStringResourceGenerator(
                    baseLocalizationRegion = iosBaseLocalizationRegion(),
                    resourcesGenerationDir = outputResourcesDir
                )
            },
            createJvm = {
                JvmStringResourceGenerator(
                    flattenClassPackage = resourcesPackageName.flatName,
                    resourcesGenerationDir = outputResourcesDir
                )
            },
            createJs = {
                JsStringResourceGenerator(
                    resourcesPackageName = resourcesPackageName,
                    resourcesGenerationDir = outputResourcesDir
                )
            },
            createWasm = {
                WasmJsStringResourceGenerator(
                    resourcesPackageName = resourcesPackageName,
                    resourcesGenerationDir = outputResourcesDir
                )
            }
        )
    }
}
