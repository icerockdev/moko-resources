/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.ClassName
import dev.icerock.gradle.generator.android.AndroidFilesGenerator
import dev.icerock.gradle.generator.apple.AppleFilesGenerator
import dev.icerock.gradle.generator.common.CommonFilesGenerator
import dev.icerock.gradle.generator.jvm.JvmFilesGenerator
import org.gradle.api.file.FileTree
import java.io.File

abstract class FilesGenerator(
    private val inputFileTree: FileTree
) : AbsFilesGenerator<AbsFilesGenerator.FileSpec>(inputFileTree) {

    override val inputFiles: Iterable<File> get() = inputFileTree.files
    override val resourceClassName = ClassName("dev.icerock.moko.resources", "FileResource")
    override val mrObjectName: String = "files"

    override fun createFileSpec(
        file: File,
        assetsGenerationDir: File,
        resourcesGenerationDir: File
    ): FileSpec {
        val key = file.nameWithoutExtension.replace("-", "_")
        return FileSpec(key, file)
    }

    class Feature(private val info: SourceInfo) : ResourceGeneratorFeature<FilesGenerator> {

        private val fileTree = info.commonResources.matching {
            it.include("MR/files/**")
        }

        override fun createCommonGenerator() = CommonFilesGenerator(fileTree)

        override fun createIosGenerator() = AppleFilesGenerator(fileTree)

        override fun createAndroidGenerator() = AndroidFilesGenerator(
            fileTree,
            info.androidRClassPackage
        )

        override fun createJvmGenerator() = JvmFilesGenerator(fileTree)
    }
}
