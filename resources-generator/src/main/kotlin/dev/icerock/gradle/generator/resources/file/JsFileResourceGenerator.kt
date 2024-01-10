/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.resources.file

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.generator.CodeConst
import dev.icerock.gradle.generator.PlatformResourceGenerator
import dev.icerock.gradle.metadata.resource.FileMetadata
import java.io.File

internal class JsFileResourceGenerator(
    private val resourcesGenerationDir: File
) : PlatformResourceGenerator<FileMetadata> {

    override fun imports(): List<ClassName> = emptyList()

    override fun generateInitializer(metadata: FileMetadata): CodeBlock {
        val requireDeclaration = """require("$FILES_DIR/${metadata.filePath.name}")"""
        return CodeBlock.of(
            "FileResource(fileUrl = js(%S) as String)",
            requireDeclaration
        )
    }

    override fun generateResourceFiles(data: List<FileMetadata>) {
        val targetDir = File(resourcesGenerationDir, FILES_DIR)
        targetDir.mkdirs()

        data.map { it.filePath }.forEach { file ->
            file.copyTo(File(targetDir, file.name))
        }
    }

    override fun generateAfterProperties(
        builder: TypeSpec.Builder,
        metadata: List<FileMetadata>
    ) {
        // FIXME duplicate
        val values: String = metadata.joinToString { it.key }

        val valuesFun: FunSpec = FunSpec.builder("values")
            .addModifiers(KModifier.OVERRIDE)
            .addStatement("return listOf($values)")
            .returns(
                ClassName("kotlin.collections", "List")
                    .parameterizedBy(CodeConst.fileResourceName)
            )
            .build()

        builder.addFunction(valuesFun)
    }

    private companion object {
        const val FILES_DIR = "files"
    }
}
