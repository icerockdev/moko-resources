/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.generator.AssetsGenerator.AssetSpec.AssetSpecDirectory
import dev.icerock.gradle.generator.AssetsGenerator.AssetSpec.AssetSpecFile
import dev.icerock.gradle.generator.android.AndroidAssetsGenerator
import dev.icerock.gradle.generator.apple.AppleAssetsGenerator
import dev.icerock.gradle.generator.common.CommonAssetsGenerator
import dev.icerock.gradle.generator.js.JsAssetsGenerator
import dev.icerock.gradle.generator.jvm.JvmAssetsGenerator
import dev.icerock.gradle.metadata.addActual
import dev.icerock.gradle.metadata.model.GeneratedObject
import dev.icerock.gradle.metadata.model.GeneratedObjectModifier
import dev.icerock.gradle.metadata.model.GeneratedProperty
import dev.icerock.gradle.metadata.model.GeneratorType
import dev.icerock.gradle.metadata.objectsWithProperties
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import org.gradle.api.Project
import org.gradle.api.file.FileTree
import java.io.File

@Suppress("TooManyFunctions")
abstract class AssetsGenerator(
    private val resourcesFileTree: FileTree,
) : MRGenerator.Generator {
    override val inputFiles: Iterable<File>
        get() = resourcesFileTree.matching {
            it.include("assets/**")
        }
    override val mrObjectName: String = ASSETS_DIR_NAME
    override val resourceClassName = ClassName("dev.icerock.moko.resources", "AssetResource")

    override val type: GeneratorType = GeneratorType.Assets

    override fun generate(
        project: Project,
        inputMetadata: MutableList<GeneratedObject>,
        generatedObjects: MutableList<GeneratedObject>,
        targetObject: GeneratedObject,
        assetsGenerationDir: File,
        resourcesGenerationDir: File,
        objectBuilder: TypeSpec.Builder,
    ): TypeSpec? {

        val previousAssetsFiles: List<File> = getPreviousAssets(
            inputMetadata = inputMetadata,
            targetObject = targetObject
        )

        val previousAssets: List<AssetSpec> = parseRootContentInner(previousAssetsFiles)
        val targetAssets: List<AssetSpec> = parseRootContentInner(inputFiles)
        val allAssets: List<AssetSpec> = (previousAssets + targetAssets)

        beforeGenerate(objectBuilder, allAssets)

        val typeSpec: TypeSpec? = createTypeSpec(
            project,
            inputMetadata = inputMetadata,
            generatedObjects = generatedObjects,
            targetObject = targetObject,
            keys = allAssets,
            objectBuilder = objectBuilder
        )

        generateResources(assetsGenerationDir, resourcesGenerationDir, allAssets)

        return typeSpec
    }

    private fun createTypeSpec(
        project: Project,
        inputMetadata: MutableList<GeneratedObject>,
        generatedObjects: MutableList<GeneratedObject>,
        targetObject: GeneratedObject,
        keys: List<AssetSpec>, objectBuilder: TypeSpec.Builder,
    ): TypeSpec? {
        if (targetObject.isActual) {
            objectBuilder.addModifiers(KModifier.ACTUAL)
        }

        if (targetObject.isActualObject || targetObject.isTargetObject) {
            extendObjectBodyAtStart(objectBuilder)
        }

        val generatedProperties = mutableListOf<GeneratedProperty>()

        createInnerTypeSpec(
            project,
            inputMetadata = inputMetadata,
            generatedProperties = generatedProperties,
            targetObject = targetObject,
            keys = keys,
            objectBuilder = objectBuilder
        )

        extendObjectBodyAtEnd(objectBuilder)

        return if (generatedProperties.isNotEmpty()) {
            // Add object in metadata with remove expect realisation
            generatedObjects.addActual(
                targetObject.copy(properties = generatedProperties)
            )

            objectBuilder.build()
        } else {
            null
        }
    }

    private fun getBaseDir(file: File): String {
        val relativePathToAssets = file.path.substringAfterLast(ASSETS_DIR_NAME)
        val fixedRelativePath = File(relativePathToAssets).path

        val result: String = if (fixedRelativePath.startsWith(File.separatorChar)) {
            fixedRelativePath.substring(1)
        } else {
            fixedRelativePath
        }

        return if (File.separatorChar == '/') result else result.replace(File.separatorChar, '/')
    }

    private fun parseRootContentInner(folders: Iterable<File>): List<AssetSpec> {
        val res = mutableListOf<AssetSpec>()

        for (it in folders) {
            if (it.isDirectory) {
                val files = it.listFiles()

                if (!files.isNullOrEmpty()) {
                    res.add(
                        AssetSpecDirectory(
                            name = it.name,
                            assets = parseRootContentInner(files.toList())
                        )
                    )
                }
            } else {
                // skip empty files, like .DS_Store
                if (it.nameWithoutExtension.isEmpty()) {
                    continue
                }

                val pathRelativeToBase = getBaseDir(it)

                if (pathRelativeToBase.contains(PATH_DELIMITER)) {
                    error("file path can't have this symbol $PATH_DELIMITER. We use them as separators.")
                }

                res.add(
                    AssetSpecFile(
                        pathRelativeToBase = pathRelativeToBase,
                        file = it
                    )
                )
            }
        }

        return res
    }

    @Suppress("SpreadOperator")
    private fun createInnerTypeSpec(
        project: Project,
        inputMetadata: MutableList<GeneratedObject>,
        generatedProperties: MutableList<GeneratedProperty>,
        targetObject: GeneratedObject,
        keys: List<AssetSpec>,
        objectBuilder: TypeSpec.Builder,
    ) {
        for (specs: AssetSpec in keys) {
            if (specs is AssetSpecDirectory) {
                val spec = TypeSpec.objectBuilder(specs.name.replace('-', '_'))

                if (targetObject.isActualObject) {
                    spec.addModifiers(KModifier.ACTUAL)
                }

                createInnerTypeSpec(
                    project,
                    inputMetadata = inputMetadata,
                    generatedProperties = generatedProperties,
                    targetObject = targetObject,
                    keys = specs.assets,
                    objectBuilder = spec
                )

                objectBuilder.addType(spec.build())
            } else if (specs is AssetSpecFile) {
                val fileName = specs.file.nameWithoutExtension.replace('-', '_')
                val styleProperty = PropertySpec.builder(fileName, resourceClassName)

                var generatedProperty = GeneratedProperty(
                    modifier = GeneratedObjectModifier.None,
                    name = fileName,
                    data = JsonPrimitive(specs.file.path)
                )

                if (targetObject.isActualObject || targetObject.isTargetObject) {
                    // Add modifier for property and setup metadata
                    generatedProperty = generatedProperty.copy(
                        modifier = addActualOverrideModifier(
                            propertyName = fileName,
                            property = styleProperty,
                            inputMetadata = inputMetadata,
                            targetObject = targetObject
                        )
                    )

                    getPropertyInitializer(specs)?.let { codeBlock ->
                        styleProperty.initializer(codeBlock)
                    }
                }

                generatedProperties.add(generatedProperty)
                objectBuilder.addProperty(styleProperty.build())
            }
        }
    }

    private fun getPreviousAssets(
        inputMetadata: List<GeneratedObject>,
        targetObject: GeneratedObject,
    ): List<File> {
        if (!targetObject.isObject || !targetObject.isActual) return emptyList()

        val json = Json
        val objectsWithProperties: List<GeneratedObject> = inputMetadata.objectsWithProperties(
            targetObject = targetObject
        )

        val files = mutableListOf<File>()

        objectsWithProperties.forEach { generatedObject ->
            generatedObject.properties.forEach { property ->
                val data = json.decodeFromJsonElement<JsonPrimitive>(property.data)
                files.add(
                    File(data.content)
                )
            }
        }

        return files
    }

    override fun getImports(): List<ClassName> = emptyList()

    protected open fun beforeGenerate(
        objectBuilder: TypeSpec.Builder,
        files: List<AssetSpec>,
    ) {
    }

    protected open fun generateResources(
        assetsGenerationDir: File,
        resourcesGenerationDir: File,
        files: List<AssetSpec>,
    ) = Unit

    abstract fun getClassModifiers(): Array<KModifier>

    abstract fun getPropertyModifiers(): Array<KModifier>

    abstract fun getPropertyInitializer(fileSpec: AssetSpecFile): CodeBlock?


    sealed class AssetSpec {
        class AssetSpecDirectory(
            val name: String,
            val assets: List<AssetSpec>,
        ) : AssetSpec()

        /**
         * @param pathRelativeToBase used to copy necessary resources in AssetsGenerator
         * @param file is a new name a of copied resource for systems which do not support path with / symbol
         */
        class AssetSpecFile(
            val pathRelativeToBase: String,
            val file: File,
        ) : AssetSpec()
    }

    class Feature(
        private val settings: MRGenerator.Settings,
    ) : ResourceGeneratorFeature<AssetsGenerator> {

        override fun createCommonGenerator(): AssetsGenerator = CommonAssetsGenerator(
            ownResourcesFileTree = settings.ownResourcesFileTree,
        )

        override fun createAppleGenerator(): AssetsGenerator = AppleAssetsGenerator(
            ownResourcesFileTree = settings.ownResourcesFileTree,
        )

        override fun createAndroidGenerator(): AssetsGenerator = AndroidAssetsGenerator(
            ownResourcesFileTree = settings.ownResourcesFileTree,
        )

        override fun createJvmGenerator(): AssetsGenerator = JvmAssetsGenerator(
            ownResourcesFileTree = settings.ownResourcesFileTree,
            settings = settings
        )

        override fun createJsGenerator(): AssetsGenerator = JsAssetsGenerator(
            ownResourcesFileTree = settings.ownResourcesFileTree,
        )
    }

    companion object {
        const val ASSETS_DIR_NAME: String = "assets"

        /*
        This is used for property name in MR class as well as a replacement of / for platforms which
        don't support it like apple.
        */
        const val PATH_DELIMITER = '+'

        val ASSETS_REGEX: Regex = "^.*/assets/.*".toRegex()
    }
}
