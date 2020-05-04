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
import dev.icerock.gradle.generator.common.CommonFilesGenerator
import dev.icerock.gradle.generator.ios.IosFilesGenerator
import org.gradle.api.file.FileTree
import java.io.File

abstract class FilesGenerator(
    private val inputFileTree: FileTree
) : MRGenerator.Generator {

    private val resourceClass = ClassName("dev.icerock.moko.resources", "FileResource")

    override fun generate(resourcesGenerationDir: File): TypeSpec {
        val fileSpecs = inputFileTree.map { file ->
            FileSpec(
                key = file.nameWithoutExtension,
                file = file
            )
        }.sortedBy { it.key }
        val typeSpec = createTypeSpec(fileSpecs.map { it.key })
        generateResources(resourcesGenerationDir, fileSpecs)
        return typeSpec
    }

    private fun createTypeSpec(keys: List<String>): TypeSpec {
        val classBuilder = TypeSpec.objectBuilder("files")
        @Suppress("SpreadOperator")
        classBuilder.addModifiers(*getClassModifiers())

        keys.forEach { classBuilder.addProperty(generateFileProperty(fileName = it)) }
        return classBuilder.build()
    }

    override fun getImports(): List<ClassName> = emptyList()

    private fun generateFileProperty(
        fileName: String
    ): PropertySpec {
        @Suppress("SpreadOperator")
        return PropertySpec.builder(fileName, resourceClass)
            .addModifiers(*getPropertyModifiers())
            .apply {
                getPropertyInitializer(fileName)?.let { initializer(it) }
            }
            .build()
    }

    protected open fun generateResources(
        resourcesGenerationDir: File,
        files: List<FileSpec>
    ) {
    }

    abstract fun getClassModifiers(): Array<KModifier>

    abstract fun getPropertyModifiers(): Array<KModifier>

    abstract fun getPropertyInitializer(fileName: String): CodeBlock?

    data class FileSpec(
        val key: String,
        val file: File
    )

    class Feature(private val info: SourceInfo) : ResourceGeneratorFeature<FilesGenerator> {
        private val fileTree = info.commonResources.matching {
            include("MR/files/**")
        }

        override fun createCommonGenerator(): FilesGenerator {
            return CommonFilesGenerator(fileTree)
        }

        override fun createIosGenerator(): FilesGenerator {
            return IosFilesGenerator(fileTree)
        }

        override fun createAndroidGenerator(): FilesGenerator {
            return AndroidFilesGenerator(
                fileTree,
                info.androidRClassPackage
            )
        }
    }
}
