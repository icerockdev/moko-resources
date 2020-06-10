/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.generator.android.AndroidImagesGenerator
import dev.icerock.gradle.generator.common.CommonImagesGenerator
import dev.icerock.gradle.generator.ios.IosImagesGenerator
import org.gradle.api.file.FileTree
import java.io.File

abstract class ImagesGenerator(
    private val inputFileTree: FileTree
) : MRGenerator.Generator {

    override fun generate(resourcesGenerationDir: File): TypeSpec {
        val keyFileMap = inputFileTree.groupBy { file ->
            file.name.substringBefore("@")
        }

        val typeSpec = createTypeSpec(keyFileMap.keys.sorted())

        generateResources(resourcesGenerationDir, keyFileMap)

        return typeSpec
    }

    @Suppress("SpreadOperator")
    fun createTypeSpec(keys: List<String>): TypeSpec {
        val classBuilder = TypeSpec.objectBuilder("images")
        classBuilder.addModifiers(*getClassModifiers())

        val resourceClass = ClassName("dev.icerock.moko.resources", "ImageResource")

        keys.forEach { key ->
            val name = key.replace(".", "_")
            val property = PropertySpec.builder(name, resourceClass)
            property.addModifiers(*getPropertyModifiers())
            getPropertyInitializer(name)?.let { property.initializer(it) }
            classBuilder.addProperty(property.build())
        }

        generateGetByFileNameMethod(classBuilder)

        return classBuilder.build()
    }

    override fun getImports(): List<ClassName> = emptyList()

    protected open fun generateResources(
        resourcesGenerationDir: File,
        keyFileMap: Map<String, List<File>>
    ) {
    }

    private fun generateGetByFileNameMethod(classBuilder: TypeSpec.Builder) {
        val imageResourceClassName = ClassName("dev.icerock.moko.resources", "ImageResource")

        val fileNameArgName = "fileName"
        val funcParams = ParameterSpec.builder(fileNameArgName, kotlin.String::class).build()

        val function = FunSpec.builder("getByFileName")
            .returns(imageResourceClassName.copy(true))
            .addParameter(funcParams)

        classBuilder.addFunction(buildGetByFileNameMethod(fileNameArgName, function).build())
    }

    protected abstract fun buildGetByFileNameMethod(argName: String, builder: FunSpec.Builder): FunSpec.Builder

    abstract fun getClassModifiers(): Array<KModifier>

    abstract fun getPropertyModifiers(): Array<KModifier>

    abstract fun getPropertyInitializer(key: String): CodeBlock?

    class Feature(private val info: SourceInfo) : ResourceGeneratorFeature<ImagesGenerator> {
        private val stringsFileTree = info.commonResources.matching {
            include("MR/images/**/*.png", "MR/images/**/*.jpg")
        }

        override fun createCommonGenerator(): ImagesGenerator {
            return CommonImagesGenerator(stringsFileTree)
        }

        override fun createIosGenerator(): ImagesGenerator {
            return IosImagesGenerator(stringsFileTree)
        }

        override fun createAndroidGenerator(): ImagesGenerator {
            return AndroidImagesGenerator(
                stringsFileTree,
                info.androidRClassPackage
            )
        }
    }
}
