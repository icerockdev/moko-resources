/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.jvm

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import dev.icerock.gradle.generator.FilesGenerator
import dev.icerock.gradle.generator.ObjectBodyExtendable
import org.gradle.api.file.FileTree
import java.io.File

class JvmFilesGenerator(
    inputFileTree: FileTree
) : FilesGenerator(inputFileTree), ObjectBodyExtendable by ClassLoaderExtender() {

    override fun getClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun getPropertyInitializer(fileSpec: FileSpec) = CodeBlock.of(
        "FileResource(resourcesClassLoader = resourcesClassLoader, filePath = %S)",
        "$FILES_DIR/${fileSpec.file.name}"
    )

    override fun generateResources(resourcesGenerationDir: File, files: List<FileSpec>) {
        val fileResDir = File(resourcesGenerationDir, FILES_DIR).apply { mkdirs() }
        files.forEach { (_, file) ->
            file.copyTo(File(fileResDir, file.name))
        }
    }

    companion object {
        private const val FILES_DIR = "files"
    }
}
