/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.ClassName
import dev.icerock.gradle.generator.android.AndroidAssetsGenerator
import dev.icerock.gradle.generator.apple.AppleAssetsGenerator
import dev.icerock.gradle.generator.common.CommonAssetsGenerator
import dev.icerock.gradle.generator.jvm.JvmAssetsGenerator
import org.gradle.api.file.FileTree
import java.io.File

abstract class AssetsGenerator(
    val inputFileTree: FileTree
) : AbsFilesGenerator<AssetsGenerator.AssetSpec>(inputFileTree) {
    override val inputFiles: Iterable<File> = inputFileTree.files
    override val mrObjectName: String = "assets"
    override val resourceClassName = ClassName("dev.icerock.moko.resources", "AssetResource")

    override fun createFileSpec(
        file: File,
        assetsGenerationDir: File,
        resourcesGenerationDir: File
    ): AssetSpec {

        if (file.path.contains('_')) {
            throw IllegalStateException("file path can't have underscore. We use them as separators.")
        }
        val pathRelativeToBase = getBaseDir(assetsGenerationDir.name, file)
        var newFilePath = pathRelativeToBase
        if (newFilePath.startsWith(File.separatorChar)) {
            newFilePath = newFilePath.substring(1)
        }
        newFilePath = newFilePath.replacePathChars()

        //remove extension and replace - symbol
        val key = newFilePath.substringBeforeLast('.').replace('-', '_')

        return AssetSpec(
            pathRelativeToBase = pathRelativeToBase,
            newFilePath = newFilePath,
            key = key,
            file
        )
    }

    private fun getBaseDir(baseDirName: String, file: File): String {
        val relativePathToAssets = file.path.substringAfterLast(baseDirName)
        return File(relativePathToAssets).path
    }

    /*
     * @param pathRelativeToBase used to copy necessary resources in AssetsGenerator
     * @param newFilePath is a new name a of copied resource for systems which do not support path with / symbol
     */
    class AssetSpec(
        val pathRelativeToBase: String,
        val newFilePath: String,
        key: String,
        file: File
    ) :
        AbsFilesGenerator.FileSpec(key, file)

    class Feature(private val info: SourceInfo) : ResourceGeneratorFeature<AssetsGenerator> {

        private val fileTree = info.commonResources.matching {
            it.include("MR/assets/**")
        }

        override fun createCommonGenerator() = CommonAssetsGenerator(fileTree)

        override fun createIosGenerator() = AppleAssetsGenerator(fileTree)

        override fun createAndroidGenerator() = AndroidAssetsGenerator(
            fileTree,
            info.androidRClassPackage
        )

        override fun createJvmGenerator() = JvmAssetsGenerator(fileTree)
    }

    private fun String.replacePathChars() = this.replace(File.separatorChar, PATH_DELIMITER)

    companion object {
        /*
        This is used for property name in MR class as well as a replacement of / for platforms which
        don't support it like apple.
         */
        private const val PATH_DELIMITER = '_'
    }
}
