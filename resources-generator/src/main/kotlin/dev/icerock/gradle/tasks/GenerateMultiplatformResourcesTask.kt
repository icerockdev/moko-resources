/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.tasks

import dev.icerock.gradle.MRVisibility
import dev.icerock.gradle.generator.PlatformContainerGenerator
import dev.icerock.gradle.generator.ResourcesFiles
import dev.icerock.gradle.generator.ResourcesGenerator
import dev.icerock.gradle.generator.container.AppleContainerGenerator
import dev.icerock.gradle.generator.container.JsContainerGenerator
import dev.icerock.gradle.generator.container.JvmContainerGenerator
import dev.icerock.gradle.generator.container.NOPContainerGenerator
import dev.icerock.gradle.generator.factory.AssetGeneratorFactory
import dev.icerock.gradle.generator.factory.ColorGeneratorFactory
import dev.icerock.gradle.generator.factory.FileGeneratorFactory
import dev.icerock.gradle.generator.factory.FontGeneratorFactory
import dev.icerock.gradle.generator.factory.ImageGeneratorFactory
import dev.icerock.gradle.generator.factory.PluralGeneratorFactory
import dev.icerock.gradle.generator.factory.StringGeneratorFactory
import dev.icerock.gradle.metadata.container.ContainerMetadata
import dev.icerock.gradle.toModifier
import dev.icerock.gradle.utils.createByPlatform
import dev.icerock.gradle.utils.isCommon
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.konan.target.KonanTarget

@CacheableTask
abstract class GenerateMultiplatformResourcesTask : DefaultTask() {

    @get:Input
    abstract val sourceSetName: Property<String>

    // not used directly in code, but required to outdate cache of target tasks when resources
    // changed
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val lowerResources: ConfigurableFileCollection

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val ownResources: ConfigurableFileCollection

    @get:Input
    abstract val upperSourceSets: MapProperty<String, FileCollection>

    @get:Optional
    @get:Input
    abstract val platformType: Property<String>

    @get:Optional
    @get:Input
    abstract val konanTarget: Property<String>

    @get:Input
    abstract val resourcesPackageName: Property<String>

    @get:Optional
    @get:Input
    abstract val appleBundleIdentifier: Property<String>

    @get:Input
    abstract val resourcesClassName: Property<String>

    @get:Optional
    @get:Input
    abstract val androidSourceSetName: Property<String>

    @get:Input
    abstract val iosBaseLocalizationRegion: Property<String>

    @get:Input
    abstract val resourcesVisibility: Property<MRVisibility>

    @get:Optional
    @get:Input
    abstract val androidRClassPackage: Property<String>

    @get:Input
    abstract val strictLineBreaks: Property<Boolean>

    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFiles
    abstract val inputMetadataFiles: ConfigurableFileCollection

    @get:OutputFile
    abstract val outputMetadataFile: RegularFileProperty

    @get:OutputDirectory
    abstract val outputResourcesDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputSourcesDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputAssetsDir: DirectoryProperty

    private val kotlinPlatformType: KotlinPlatformType
        get() = KotlinPlatformType.valueOf(platformType.get())

    private val kotlinKonanTarget: KonanTarget
        get() {
            val name: String = konanTarget.get()
            return KonanTarget.predefinedTargets[name] ?: error("can't find $name in KonanTarget")
        }

    init {
        group = "moko-resources"
    }

    @TaskAction
    fun generate() {
        // cleanup destinations
        listOf(outputResourcesDir, outputSourcesDir, outputAssetsDir, outputMetadataFile).forEach {
            it.get().asFile.deleteRecursively()
        }

        val json = Json {
            prettyPrint = true
        }
        val generator: ResourcesGenerator = createGenerator()

        val files = ResourcesFiles(
            ownSourceSet = ResourcesFiles.SourceSetResources(
                sourceSetName = sourceSetName.get(),
                fileTree = ownResources.asFileTree
            ),
            upperSourceSets = upperSourceSets.get().map { item ->
                ResourcesFiles.SourceSetResources(
                    sourceSetName = item.key,
                    fileTree = item.value.asFileTree
                )
            }
        )
        val serializer: KSerializer<List<ContainerMetadata>> =
            ListSerializer(ContainerMetadata.serializer())
        val inputMetadata: List<ContainerMetadata> = inputMetadataFiles.files.flatMap { file ->
            json.decodeFromString(serializer, file.readText())
        }

        val outputMetadata: List<ContainerMetadata> = if (kotlinPlatformType.isCommon) {
            generator.generateCommonKotlin(files, inputMetadata)
        } else {
            generator.generateTargetKotlin(files, inputMetadata).also { containers ->
                generator.generateResources(containers)
            }
        }

        outputMetadataFile.get().asFile.writeText(json.encodeToString(serializer, outputMetadata))
    }

    private fun createGenerator(): ResourcesGenerator {
        return ResourcesGenerator(
            containerGenerator = createPlatformContainerGenerator(),
            typesGenerators = createTypeGenerators(),
            resourcesPackageName = resourcesPackageName.get(),
            resourcesClassName = resourcesClassName.get(),
            sourceSetName = sourceSetName.get(),
            visibilityModifier = resourcesVisibility.get().toModifier(),
            sourcesGenerationDir = outputSourcesDir.get().asFile,
            logger = logger
        )
    }

    private fun createPlatformContainerGenerator(): PlatformContainerGenerator {
        return createByPlatform(
            kotlinPlatformType = kotlinPlatformType,
            konanTarget = ::kotlinKonanTarget,
            createCommon = { NOPContainerGenerator() },
            createAndroid = { NOPContainerGenerator() },
            createJs = { JsContainerGenerator() },
            createApple = {
                AppleContainerGenerator(
                    bundleIdentifier = appleBundleIdentifier.get()
                )
            },
            createJvm = {
                JvmContainerGenerator()
            }
        )
    }

    @Suppress("LongMethod")
    private fun createTypeGenerators() = listOf(
        StringGeneratorFactory(
            resourcesPackageName = resourcesPackageName.get(),
            resourcesVisibility = resourcesVisibility.get(),
            strictLineBreaks = strictLineBreaks.get(),
            outputResourcesDir = outputResourcesDir.get().asFile,
            kotlinPlatformType = kotlinPlatformType,
            kotlinKonanTarget = ::kotlinKonanTarget,
            androidRClassPackage = androidRClassPackage::get,
            iosBaseLocalizationRegion = iosBaseLocalizationRegion::get,
        ).create(),
        PluralGeneratorFactory(
            resourcesPackageName = resourcesPackageName.get(),
            resourcesVisibility = resourcesVisibility.get(),
            strictLineBreaks = strictLineBreaks.get(),
            outputResourcesDir = outputResourcesDir.get().asFile,
            kotlinPlatformType = kotlinPlatformType,
            kotlinKonanTarget = ::kotlinKonanTarget,
            androidRClassPackage = androidRClassPackage::get,
            iosBaseLocalizationRegion = iosBaseLocalizationRegion::get,
        ).create(),
        ImageGeneratorFactory(
            resourcesPackageName = resourcesPackageName.get(),
            resourcesVisibility = resourcesVisibility.get(),
            outputResourcesDir = outputResourcesDir.get().asFile,
            outputAssetsDir = outputAssetsDir.get().asFile,
            kotlinPlatformType = kotlinPlatformType,
            kotlinKonanTarget = ::kotlinKonanTarget,
            androidRClassPackage = androidRClassPackage::get,
            logger = logger
        ).create(),
        ColorGeneratorFactory(
            resourcesPackageName = resourcesPackageName.get(),
            resourcesVisibility = resourcesVisibility.get(),
            outputResourcesDir = outputResourcesDir.get().asFile,
            outputAssetsDir = outputAssetsDir.get().asFile,
            kotlinPlatformType = kotlinPlatformType,
            kotlinKonanTarget = ::kotlinKonanTarget,
            androidRClassPackage = androidRClassPackage::get,
        ).create(),
        FontGeneratorFactory(
            resourcesPackageName = resourcesPackageName.get(),
            resourcesVisibility = resourcesVisibility.get(),
            outputResourcesDir = outputResourcesDir.get().asFile,
            kotlinPlatformType = kotlinPlatformType,
            kotlinKonanTarget = ::kotlinKonanTarget,
            androidRClassPackage = androidRClassPackage::get,
        ).create(),
        FileGeneratorFactory(
            resourcesPackageName = resourcesPackageName.get(),
            resourcesVisibility = resourcesVisibility.get(),
            outputResourcesDir = outputResourcesDir.get().asFile,
            kotlinPlatformType = kotlinPlatformType,
            kotlinKonanTarget = ::kotlinKonanTarget,
            androidRClassPackage = androidRClassPackage::get,
            ownResources = ownResources
        ).create(),
        AssetGeneratorFactory(
            resourcesPackageName = resourcesPackageName.get(),
            resourcesVisibility = resourcesVisibility.get(),
            outputResourcesDir = outputResourcesDir.get().asFile,
            outputAssetsDir = outputAssetsDir.get().asFile,
            kotlinPlatformType = kotlinPlatformType,
            kotlinKonanTarget = ::kotlinKonanTarget,
            androidRClassPackage = androidRClassPackage::get,
            ownResources = ownResources
        ).create()
    )
}
