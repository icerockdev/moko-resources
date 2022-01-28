/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.generator.android.AndroidImagesGenerator
import dev.icerock.gradle.generator.apple.AppleImagesGenerator
import dev.icerock.gradle.generator.js.JsImagesGenerator
import dev.icerock.gradle.generator.common.CommonImagesGenerator
import dev.icerock.gradle.generator.jvm.JvmImagesGenerator
import org.gradle.api.file.FileTree
import java.io.File

abstract class ImagesGenerator(
    private val inputFileTree: FileTree
) : MRGenerator.Generator {

    override val inputFiles: Iterable<File> get() = inputFileTree.files
    override val resourceClassName = ClassName("dev.icerock.moko.resources", "ImageResource")
    override val mrObjectName: String = "images"

    override fun generate(
        assetsGenerationDir: File,
        resourcesGenerationDir: File,
        objectBuilder: TypeSpec.Builder
    ): TypeSpec {
        val fileMap = inputFileTree.groupBy { file ->
            "${file.name.substringBefore("@")}.${file.extension}"
        }

        val typeSpec = createTypeSpec(fileMap.keys.sorted(), objectBuilder)

        generateResources(
            resourcesGenerationDir,
            fileMap.mapKeys { (key, _) ->
                key.substringBeforeLast(".") // Remove file extension from keys
            })

        return typeSpec
    }

    @Suppress("SpreadOperator")
    fun createTypeSpec(fileNames: List<String>, objectBuilder: TypeSpec.Builder): TypeSpec {
        objectBuilder.addModifiers(*getClassModifiers())

        extendObjectBodyAtStart(objectBuilder)

        fileNames.forEach { fileName ->
            val updatedFileName = fileName.substringBeforeLast(".")
                .replace(".", "_") + ".${fileName.substringAfterLast(".")}"
            val propertyName = updatedFileName.substringBeforeLast(".")
            val property = PropertySpec.builder(propertyName, resourceClassName)

            property.addModifiers(*getPropertyModifiers())
            getPropertyInitializer(updatedFileName)?.let { property.initializer(it) }
            objectBuilder.addProperty(property.build())
        }

        extendObjectBodyAtEnd(objectBuilder)

        return objectBuilder.build()
    }

    override fun getImports(): List<ClassName> = emptyList()

    protected open fun generateResources(
        resourcesGenerationDir: File,
        keyFileMap: Map<String, List<File>>
    ) {
    }

    abstract fun getClassModifiers(): Array<KModifier>

    abstract fun getPropertyModifiers(): Array<KModifier>

    abstract fun getPropertyInitializer(fileName: String): CodeBlock?

    class Feature(
        private val info: SourceInfo,
        private val mrSettings: MRGenerator.MRSettings
    ) : ResourceGeneratorFeature<ImagesGenerator> {
        private val stringsFileTree = info.commonResources.matching {
            it.include("MR/images/**/*.png", "MR/images/**/*.jpg")
        }

        override fun createCommonGenerator() =
            CommonImagesGenerator(stringsFileTree)

        override fun createIosGenerator() = AppleImagesGenerator(stringsFileTree)

        override fun createAndroidGenerator() = AndroidImagesGenerator(
            stringsFileTree,
            info.androidRClassPackage
        )

        override fun createJsGenerator(): ImagesGenerator = JsImagesGenerator(stringsFileTree)

        override fun createJvmGenerator() = JvmImagesGenerator(
            stringsFileTree,
            mrSettings
        )
    }
}
