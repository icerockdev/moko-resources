/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.tasks

import dev.icerock.gradle.MRVisibility
import dev.icerock.gradle.generator.CodeConst
import dev.icerock.gradle.generator.PlatformContainerGenerator
import dev.icerock.gradle.generator.PlatformResourceGenerator
import dev.icerock.gradle.generator.ResourceTypeGenerator
import dev.icerock.gradle.generator.ResourcesFiles
import dev.icerock.gradle.generator.ResourcesGenerator
import dev.icerock.gradle.generator.container.AppleContainerGenerator
import dev.icerock.gradle.generator.container.JvmContainerGenerator
import dev.icerock.gradle.generator.container.NOPContainerGenerator
import dev.icerock.gradle.generator.resources.NOPResourceGenerator
import dev.icerock.gradle.generator.resources.color.AndroidColorResourceGenerator
import dev.icerock.gradle.generator.resources.color.AppleColorResourceGenerator
import dev.icerock.gradle.generator.resources.color.ColorResourceGenerator
import dev.icerock.gradle.generator.resources.color.JsColorResourceGenerator
import dev.icerock.gradle.generator.resources.color.JvmColorResourceGenerator
import dev.icerock.gradle.generator.resources.file.AndroidFileResourceGenerator
import dev.icerock.gradle.generator.resources.file.AppleFileResourceGenerator
import dev.icerock.gradle.generator.resources.file.FileResourceGenerator
import dev.icerock.gradle.generator.resources.file.JsFileResourceGenerator
import dev.icerock.gradle.generator.resources.file.JvmFileResourceGenerator
import dev.icerock.gradle.generator.resources.font.AndroidFontResourceGenerator
import dev.icerock.gradle.generator.resources.font.AppleFontResourceGenerator
import dev.icerock.gradle.generator.resources.font.FontResourceGenerator
import dev.icerock.gradle.generator.resources.font.JsFontResourceGenerator
import dev.icerock.gradle.generator.resources.font.JvmFontResourceGenerator
import dev.icerock.gradle.generator.resources.image.AndroidImageResourceGenerator
import dev.icerock.gradle.generator.resources.image.AppleImageResourceGenerator
import dev.icerock.gradle.generator.resources.image.ImageResourceGenerator
import dev.icerock.gradle.generator.resources.image.JsImageResourceGenerator
import dev.icerock.gradle.generator.resources.image.JvmImageResourceGenerator
import dev.icerock.gradle.generator.resources.plural.AndroidPluralResourceGenerator
import dev.icerock.gradle.generator.resources.plural.ApplePluralResourceGenerator
import dev.icerock.gradle.generator.resources.plural.JsPluralResourceGenerator
import dev.icerock.gradle.generator.resources.plural.JvmPluralResourceGenerator
import dev.icerock.gradle.generator.resources.plural.PluralResourceGenerator
import dev.icerock.gradle.generator.resources.string.AndroidStringResourceGenerator
import dev.icerock.gradle.generator.resources.string.AppleStringResourceGenerator
import dev.icerock.gradle.generator.resources.string.JsStringResourceGenerator
import dev.icerock.gradle.generator.resources.string.JvmStringResourceGenerator
import dev.icerock.gradle.generator.resources.string.StringResourceGenerator
import dev.icerock.gradle.metadata.container.ContainerMetadata
import dev.icerock.gradle.metadata.container.ObjectMetadata
import dev.icerock.gradle.metadata.container.ResourceType
import dev.icerock.gradle.metadata.resource.ColorMetadata
import dev.icerock.gradle.metadata.resource.FileMetadata
import dev.icerock.gradle.metadata.resource.FontMetadata
import dev.icerock.gradle.metadata.resource.ImageMetadata
import dev.icerock.gradle.metadata.resource.PluralMetadata
import dev.icerock.gradle.metadata.resource.StringMetadata
import dev.icerock.gradle.toModifier
import dev.icerock.gradle.utils.createByPlatform
import dev.icerock.gradle.utils.flatName
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
import org.gradle.api.tasks.Classpath
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
import java.io.File

@CacheableTask
abstract class GenerateMultiplatformResourcesTask : DefaultTask() {

    @get:Input
    abstract val sourceSetName: Property<String>

    @get:InputFiles
    @get:Classpath
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

    @get:OutputFile
    abstract val outputMetadataFile: RegularFileProperty

    @get:Optional
    @get:PathSensitive(PathSensitivity.ABSOLUTE)
    @get:InputFiles
    abstract val inputMetadataFiles: ConfigurableFileCollection

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

        val outputMetadata: List<ContainerMetadata> =
            if (kotlinPlatformType == KotlinPlatformType.common) {
                generator.generateCommonKotlin(files, inputMetadata)
            } else {
                generator.generateTargetKotlin(files, inputMetadata).also { containers ->
                    generator.generateResources(containers.mapNotNull { it as? ObjectMetadata })
                }
            }

        outputMetadataFile.get().asFile.writeText(json.encodeToString(serializer, outputMetadata))
    }

    private fun createGenerator(): ResourcesGenerator {
        return ResourcesGenerator(
            containerGenerator = createPlatformContainerGenerator(),
            typesGenerators = listOf(
                createStringGenerator(),
                createPluralsGenerator(),
                createImagesGenerator(),
                createColorsGenerator(),
                createFontsGenerator(),
                createFilesGenerator()
            ),
            resourcesPackageName = resourcesPackageName.get(),
            resourcesClassName = resourcesClassName.get(),
            sourceSetName = sourceSetName.get(),
            visibilityModifier = resourcesVisibility.get().toModifier(),
            sourcesGenerationDir = outputSourcesDir.get().asFile
        )
    }

    private fun createPlatformContainerGenerator(): PlatformContainerGenerator {
        return createByPlatform(
            kotlinPlatformType = kotlinPlatformType,
            konanTarget = ::kotlinKonanTarget,
            createCommon = { NOPContainerGenerator() },
            createAndroid = { NOPContainerGenerator() },
            createJs = { NOPContainerGenerator() },
            createApple = {
                AppleContainerGenerator(
                    bundleIdentifier = "${resourcesPackageName.get()}.MR"
                )
            },
            createJvm = {
                JvmContainerGenerator(
                    resourcesClassName = resourcesClassName.get()
                )
            }
        )
    }

    private fun createStringGenerator(): ResourceTypeGenerator<StringMetadata> {
        return ResourceTypeGenerator(
            generationPackage = resourcesPackageName.get(),
            resourceClass = CodeConst.stringResourceName,
            resourceType = ResourceType.STRINGS,
            metadataClass = StringMetadata::class,
            visibilityModifier = resourcesVisibility.get().toModifier(),
            generator = StringResourceGenerator(
                strictLineBreaks = strictLineBreaks.get()
            ),
            platformResourceGenerator = createPlatformStringGenerator(),
            filter = { include("**/strings*.xml") }
        )
    }

    private fun createPluralsGenerator(): ResourceTypeGenerator<PluralMetadata> {
        return ResourceTypeGenerator(
            generationPackage = resourcesPackageName.get(),
            resourceClass = CodeConst.pluralsResourceName,
            resourceType = ResourceType.PLURALS,
            metadataClass = PluralMetadata::class,
            visibilityModifier = resourcesVisibility.get().toModifier(),
            generator = PluralResourceGenerator(
                strictLineBreaks = strictLineBreaks.get()
            ),
            platformResourceGenerator = createPlatformPluralGenerator(),
            filter = { include("**/plurals*.xml") }
        )
    }

    private fun createImagesGenerator(): ResourceTypeGenerator<ImageMetadata> {
        return ResourceTypeGenerator(
            generationPackage = resourcesPackageName.get(),
            resourceClass = CodeConst.imageResourceName,
            resourceType = ResourceType.IMAGES,
            metadataClass = ImageMetadata::class,
            visibilityModifier = resourcesVisibility.get().toModifier(),
            generator = ImageResourceGenerator(),
            platformResourceGenerator = createPlatformImageGenerator(),
            filter = {
                include("images/**/*.png", "images/**/*.jpg", "images/**/*.svg")
            }
        )
    }

    private fun createColorsGenerator(): ResourceTypeGenerator<ColorMetadata> {
        return ResourceTypeGenerator(
            generationPackage = resourcesPackageName.get(),
            resourceClass = CodeConst.colorResourceName,
            resourceType = ResourceType.COLORS,
            metadataClass = ColorMetadata::class,
            visibilityModifier = resourcesVisibility.get().toModifier(),
            generator = ColorResourceGenerator(),
            platformResourceGenerator = createPlatformColorGenerator(),
            filter = { include("**/colors*.xml") }
        )
    }

    private fun createFontsGenerator(): ResourceTypeGenerator<FontMetadata> {
        return ResourceTypeGenerator(
            generationPackage = resourcesPackageName.get(),
            resourceClass = CodeConst.fontResourceName,
            resourceType = ResourceType.FONTS,
            metadataClass = FontMetadata::class,
            visibilityModifier = resourcesVisibility.get().toModifier(),
            generator = FontResourceGenerator(),
            platformResourceGenerator = createPlatformFontGenerator(),
            filter = { include("fonts/**.ttf", "fonts/**.otf") }
        )
    }

    private fun createFilesGenerator(): ResourceTypeGenerator<FileMetadata> {
        return ResourceTypeGenerator(
            generationPackage = resourcesPackageName.get(),
            resourceClass = CodeConst.fileResourceName,
            resourceType = ResourceType.FILES,
            metadataClass = FileMetadata::class,
            visibilityModifier = resourcesVisibility.get().toModifier(),
            generator = FileResourceGenerator(),
            platformResourceGenerator = createPlatformFileGenerator(),
            filter = { include("files/**") }
        )
    }

    private fun createPlatformFileGenerator(): PlatformResourceGenerator<FileMetadata> {
        val resourcesGenerationDir: File = outputResourcesDir.get().asFile
        return createByPlatform(
            kotlinPlatformType = kotlinPlatformType,
            konanTarget = ::kotlinKonanTarget,
            // TODO find way to remove this NOP
            createCommon = { NOPResourceGenerator() },
            createAndroid = {
                AndroidFileResourceGenerator(
                    androidRClassPackage = androidRClassPackage.get(),
                    resourcesGenerationDir = resourcesGenerationDir
                )
            },
            createApple = {
                AppleFileResourceGenerator(
                    resourcesGenerationDir = resourcesGenerationDir
                )
            },
            createJvm = {
                JvmFileResourceGenerator(
                    className = resourcesClassName.get(),
                    resourcesGenerationDir = resourcesGenerationDir
                )
            },
            createJs = {
                JsFileResourceGenerator(
                    resourcesGenerationDir = resourcesGenerationDir
                )
            }
        )
    }

    private fun createPlatformFontGenerator(): PlatformResourceGenerator<FontMetadata> {
        val resourcesGenerationDir: File = outputResourcesDir.get().asFile
        return createByPlatform(
            kotlinPlatformType = kotlinPlatformType,
            konanTarget = ::kotlinKonanTarget,
            // TODO find way to remove this NOP
            createCommon = { NOPResourceGenerator() },
            createAndroid = {
                AndroidFontResourceGenerator(
                    androidRClassPackage = androidRClassPackage.get(),
                    resourcesGenerationDir = resourcesGenerationDir
                )
            },
            createApple = {
                AppleFontResourceGenerator(
                    resourcesGenerationDir = resourcesGenerationDir
                )
            },
            createJvm = {
                JvmFontResourceGenerator(
                    className = resourcesClassName.get(),
                    resourcesGenerationDir = resourcesGenerationDir
                )
            },
            createJs = {
                JsFontResourceGenerator(
                    resourcesPackageName = resourcesPackageName.get(),
                    resourcesGenerationDir = resourcesGenerationDir
                )
            }
        )
    }

    private fun createPlatformColorGenerator(): PlatformResourceGenerator<ColorMetadata> {
        val resourcesGenerationDir: File = outputResourcesDir.get().asFile
        val assetsGenerationDir: File = outputAssetsDir.get().asFile
        return createByPlatform(
            kotlinPlatformType = kotlinPlatformType,
            konanTarget = ::kotlinKonanTarget,
            // TODO find way to remove this NOP
            createCommon = { NOPResourceGenerator() },
            createAndroid = {
                AndroidColorResourceGenerator(
                    androidRClassPackage = androidRClassPackage.get(),
                    resourcesGenerationDir = resourcesGenerationDir
                )
            },
            createApple = {
                AppleColorResourceGenerator(
                    assetsGenerationDir = assetsGenerationDir
                )
            },
            createJvm = {
                JvmColorResourceGenerator(
                    className = resourcesClassName.get()
                )
            },
            createJs = {
                JsColorResourceGenerator()
            }
        )
    }

    private fun createPlatformImageGenerator(): PlatformResourceGenerator<ImageMetadata> {
        val resourcesGenerationDir: File = outputResourcesDir.get().asFile
        val assetsGenerationDir: File = outputAssetsDir.get().asFile
        return createByPlatform(
            kotlinPlatformType = kotlinPlatformType,
            konanTarget = ::kotlinKonanTarget,
            // TODO find way to remove this NOP
            createCommon = { NOPResourceGenerator() },
            createAndroid = {
                AndroidImageResourceGenerator(
                    androidRClassPackage = androidRClassPackage.get(),
                    resourcesGenerationDir = resourcesGenerationDir,
                    logger = this.logger
                )
            },
            createApple = {
                AppleImageResourceGenerator(
                    assetsGenerationDir = assetsGenerationDir
                )
            },
            createJvm = {
                JvmImageResourceGenerator(
                    className = resourcesClassName.get(),
                    resourcesGenerationDir = resourcesGenerationDir
                )
            },
            createJs = {
                JsImageResourceGenerator(
                    resourcesGenerationDir = resourcesGenerationDir
                )
            }
        )
    }

    private fun createPlatformPluralGenerator(): PlatformResourceGenerator<PluralMetadata> {
        val resourcesGenerationDir: File = outputResourcesDir.get().asFile
        return createByPlatform(
            kotlinPlatformType = kotlinPlatformType,
            konanTarget = ::kotlinKonanTarget,
            // TODO find way to remove this NOP
            createCommon = { NOPResourceGenerator() },
            createAndroid = {
                AndroidPluralResourceGenerator(
                    androidRClassPackage = androidRClassPackage.get(),
                    resourcesGenerationDir = resourcesGenerationDir
                )
            },
            createApple = {
                ApplePluralResourceGenerator(
                    baseLocalizationRegion = iosBaseLocalizationRegion.get(),
                    resourcesGenerationDir = resourcesGenerationDir
                )
            },
            createJvm = {
                JvmPluralResourceGenerator(
                    flattenClassPackage = resourcesPackageName.get().flatName,
                    className = resourcesClassName.get(),
                    resourcesGenerationDir = resourcesGenerationDir
                )
            },
            createJs = {
                JsPluralResourceGenerator(
                    resourcesPackageName = resourcesPackageName.get(),
                    resourcesGenerationDir = resourcesGenerationDir
                )
            }
        )
    }

    private fun createPlatformStringGenerator(): PlatformResourceGenerator<StringMetadata> {
        val resourcesGenerationDir: File = outputResourcesDir.get().asFile
        return createByPlatform(
            kotlinPlatformType = kotlinPlatformType,
            konanTarget = ::kotlinKonanTarget,
            // TODO find way to remove this NOP
            createCommon = { NOPResourceGenerator() },
            createAndroid = {
                AndroidStringResourceGenerator(
                    androidRClassPackage = androidRClassPackage.get(),
                    resourcesGenerationDir = resourcesGenerationDir
                )
            },
            createApple = {
                AppleStringResourceGenerator(
                    baseLocalizationRegion = iosBaseLocalizationRegion.get(),
                    resourcesGenerationDir = resourcesGenerationDir
                )
            },
            createJvm = {
                JvmStringResourceGenerator(
                    flattenClassPackage = resourcesPackageName.get().flatName,
                    className = resourcesClassName.get(),
                    resourcesGenerationDir = resourcesGenerationDir
                )
            },
            createJs = {
                JsStringResourceGenerator(
                    resourcesPackageName = resourcesPackageName.get(),
                    resourcesGenerationDir = resourcesGenerationDir
                )
            }
        )
    }
}
