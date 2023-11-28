/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.generator.android.AndroidFilesGenerator
import dev.icerock.gradle.generator.apple.AppleFilesGenerator
import dev.icerock.gradle.generator.common.CommonFilesGenerator
import dev.icerock.gradle.generator.js.JsFilesGenerator
import dev.icerock.gradle.generator.jvm.JvmFilesGenerator
import dev.icerock.gradle.metadata.GeneratedObject
import org.gradle.api.file.FileTree
import java.io.File

abstract class FilesGenerator(
    private val inputFileTree: FileTree
) : MRGenerator.Generator {

    override val inputFiles: Iterable<File> get() = inputFileTree.files
    override val resourceClassName = ClassName("dev.icerock.moko.resources", "FileResource")
    override val mrObjectName: String = "files"

    override fun generate(
        metadata: List<GeneratedObject>,
        typeSpecIsInterface: Boolean,
        assetsGenerationDir: File,
        resourcesGenerationDir: File,
        objectBuilder: TypeSpec.Builder
    ): TypeSpec {
        val fileSpecs = inputFileTree.map { file ->
            FileSpec(
                key = processKey(file.nameWithoutExtension),
                file = file
            )
        }.sortedBy { it.key }
        beforeGenerate(objectBuilder, fileSpecs)
        val typeSpec = createTypeSpec(fileSpecs, objectBuilder)
        generateResources(resourcesGenerationDir, fileSpecs)
        return typeSpec
    }

    private fun createTypeSpec(keys: List<FileSpec>, objectBuilder: TypeSpec.Builder): TypeSpec {
        @Suppress("SpreadOperator")
        objectBuilder.addModifiers(*getClassModifiers())

        extendObjectBodyAtStart(objectBuilder)

        keys.forEach { objectBuilder.addProperty(generateFileProperty(it)) }
        extendObjectBodyAtEnd(objectBuilder)
        return objectBuilder.build()
    }

    override fun getImports(): List<ClassName> = emptyList()

    private fun generateFileProperty(
        fileSpec: FileSpec
    ): PropertySpec {
        @Suppress("SpreadOperator")
        return PropertySpec.builder(fileSpec.key, resourceClassName)
            .addModifiers(*getPropertyModifiers())
            .apply {
                getPropertyInitializer(fileSpec)?.let { initializer(it) }
            }
            .build()
    }

    protected open fun beforeGenerate(
        objectBuilder: TypeSpec.Builder,
        files: List<FileSpec>
    ) = Unit

    protected open fun generateResources(
        resourcesGenerationDir: File,
        files: List<FileSpec>
    ) = Unit

    private fun processKey(key: String): String {
        return key.replace("-", "_")
    }

    abstract fun getClassModifiers(): Array<KModifier>

    abstract fun getPropertyModifiers(): Array<KModifier>

    abstract fun getPropertyInitializer(fileSpec: FileSpec): CodeBlock?

    data class FileSpec(
        val key: String,
        val file: File
    )

    class Feature(
        private val settings: MRGenerator.Settings
    ) : ResourceGeneratorFeature<FilesGenerator> {

//        private val fileTree: FileTree = settings.ownResourcesFileTree
//            .matching { it.include("files/**") }

        override fun createCommonGenerator(): FilesGenerator = CommonFilesGenerator(
            ownInputFileTree = settings.ownResourcesFileTree,
            upperInputFileTree = settings.upperResourcesFileTree
        )

        override fun createIosGenerator(): FilesGenerator = AppleFilesGenerator(
            ownInputFileTree = settings.ownResourcesFileTree,
            lowerInputFileTree = settings.lowerResourcesFileTree
        )

        override fun createAndroidGenerator(): FilesGenerator = AndroidFilesGenerator(
            ownInputFileTree = settings.ownResourcesFileTree,
            lowerInputFileTree = settings.lowerResourcesFileTree,
            androidRClassPackage = settings.androidRClassPackage,
        )

        override fun createJsGenerator(): FilesGenerator = JsFilesGenerator(
            ownInputFileTree = settings.ownResourcesFileTree,
            lowerInputFileTree = settings.lowerResourcesFileTree
        )

        override fun createJvmGenerator(): FilesGenerator = JvmFilesGenerator(
            ownInputFileTree = settings.ownResourcesFileTree,
            lowerInputFileTree = settings.lowerResourcesFileTree,
            settings = settings
        )
    }
}
