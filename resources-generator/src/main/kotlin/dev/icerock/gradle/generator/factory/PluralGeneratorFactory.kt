/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.factory

import dev.icerock.gradle.MRVisibility
import dev.icerock.gradle.generator.Constants
import dev.icerock.gradle.generator.FlatPropertiesGenerationStrategy
import dev.icerock.gradle.generator.PlatformResourceGenerator
import dev.icerock.gradle.generator.ResourceTypeGenerator
import dev.icerock.gradle.generator.platform.js.JsFilePathMode
import dev.icerock.gradle.generator.resources.NOPResourceGenerator
import dev.icerock.gradle.generator.resources.plural.AndroidPluralResourceGenerator
import dev.icerock.gradle.generator.resources.plural.ApplePluralResourceGenerator
import dev.icerock.gradle.generator.resources.plural.JsPluralResourceGenerator
import dev.icerock.gradle.generator.resources.plural.JvmPluralResourceGenerator
import dev.icerock.gradle.generator.resources.plural.PluralResourceGenerator
import dev.icerock.gradle.metadata.container.ResourceType
import dev.icerock.gradle.metadata.resource.PluralMetadata
import dev.icerock.gradle.toModifier
import dev.icerock.gradle.utils.createByPlatform
import dev.icerock.gradle.utils.flatName
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File

@Suppress("LongParameterList")
internal class PluralGeneratorFactory(
    private val resourcesPackageName: String,
    private val resourcesVisibility: MRVisibility,
    private val strictLineBreaks: Boolean,
    private val outputResourcesDir: File,
    private val kotlinPlatformType: KotlinPlatformType,
    private val kotlinKonanTarget: () -> KonanTarget,
    private val androidRClassPackage: () -> String,
    private val iosBaseLocalizationRegion: () -> String
) {
    fun create(): ResourceTypeGenerator<PluralMetadata> {
        return ResourceTypeGenerator(
            propertiesGenerationStrategy = FlatPropertiesGenerationStrategy(),
            resourceClass = Constants.pluralsResourceName,
            resourceType = ResourceType.PLURALS,
            metadataClass = PluralMetadata::class,
            visibilityModifier = resourcesVisibility.toModifier(),
            generator = PluralResourceGenerator(
                strictLineBreaks = strictLineBreaks
            ),
            platformResourceGenerator = createPlatformPluralGenerator(),
            filter = { include("**/plurals*.xml") }
        )
    }

    private fun createPlatformPluralGenerator(): PlatformResourceGenerator<PluralMetadata> {
        return createByPlatform(
            kotlinPlatformType = kotlinPlatformType,
            konanTarget = kotlinKonanTarget,
            // TODO find way to remove this NOP
            createCommon = { NOPResourceGenerator() },
            createAndroid = {
                AndroidPluralResourceGenerator(
                    androidRClassPackage = androidRClassPackage(),
                    resourcesGenerationDir = outputResourcesDir
                )
            },
            createApple = {
                ApplePluralResourceGenerator(
                    baseLocalizationRegion = iosBaseLocalizationRegion(),
                    resourcesGenerationDir = outputResourcesDir
                )
            },
            createJvm = {
                JvmPluralResourceGenerator(
                    flattenClassPackage = resourcesPackageName.flatName,
                    resourcesGenerationDir = outputResourcesDir
                )
            },
            createJs = {
                JsPluralResourceGenerator(
                    resourcesPackageName = resourcesPackageName,
                    resourcesGenerationDir = outputResourcesDir,
                    filePathMode = JsFilePathMode.require
                )
            },
            createWasm = {
                JsPluralResourceGenerator(
                    resourcesPackageName = resourcesPackageName,
                    resourcesGenerationDir = outputResourcesDir,
                    filePathMode = JsFilePathMode.rawPath
                )
            }
        )
    }
}
