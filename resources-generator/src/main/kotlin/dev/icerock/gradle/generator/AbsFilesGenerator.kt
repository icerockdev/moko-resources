/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import org.gradle.api.file.FileTree
import java.io.File

abstract class AbsFilesGenerator<FSpec : AbsFilesGenerator.FileSpec>(
    private val inputFileTree: FileTree
) : MRGenerator.Generator {

    override fun generate(
        assetsGenerationDir: File,
        resourcesGenerationDir: File,
        objectBuilder: TypeSpec.Builder
    ): TypeSpec {
        val fileSpecs = inputFileTree.map { file ->
            createFileSpec(file, assetsGenerationDir, resourcesGenerationDir)
        }.sortedBy { it.key }
        val typeSpec = createTypeSpec(fileSpecs, objectBuilder)
        generateResources(assetsGenerationDir, resourcesGenerationDir, fileSpecs)
        return typeSpec
    }

    private fun createTypeSpec(keys: List<FSpec>, objectBuilder: TypeSpec.Builder): TypeSpec {
        @Suppress("SpreadOperator")
        objectBuilder.addModifiers(*getClassModifiers())

        extendObjectBodyAtStart(objectBuilder)

        keys.forEach { objectBuilder.addProperty(generateFileProperty(it)) }
        extendObjectBodyAtEnd(objectBuilder)
        return objectBuilder.build()
    }

    override fun getImports(): List<ClassName> = emptyList()

    private fun generateFileProperty(
        fileSpec: FSpec
    ): PropertySpec {
        @Suppress("SpreadOperator")
        return PropertySpec.builder(fileSpec.key, resourceClassName)
            .addModifiers(*getPropertyModifiers())
            .apply {
                getPropertyInitializer(fileSpec)?.let { initializer(it) }
            }
            .build()
    }


    abstract fun getClassModifiers(): Array<KModifier>

    abstract fun getPropertyModifiers(): Array<KModifier>

    abstract fun getPropertyInitializer(fileSpec: FSpec): CodeBlock?

    abstract fun createFileSpec(
        file: File,
        assetsGenerationDir: File,
        resourcesGenerationDir: File
    ): FSpec


    protected open fun generateResources(
        assetsGenerationDir: File,
        resourcesGenerationDir: File,
        files: List<FSpec>
    ) {
    }

    open class FileSpec(
        //used to generate property name in MR class
        val key: String,
        val file: File
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is FileSpec) return false

            if (file != other.file) return false

            return true
        }

        override fun hashCode(): Int {
            return file.hashCode()
        }
    }
}