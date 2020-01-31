/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.image

import com.squareup.kotlinpoet.*
import dev.icerock.gradle.generator.MRGenerator
import org.gradle.api.file.FileTree
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.io.File

abstract class ImagesGenerator(
    protected val sourceSet: KotlinSourceSet,
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

        return classBuilder.build()
    }

    override fun getImports(): List<ClassName> = emptyList()

    protected open fun generateResources(
        resourcesGenerationDir: File,
        keyFileMap: Map<String, List<File>>
    ) {
    }

    abstract fun getClassModifiers(): Array<KModifier>

    abstract fun getPropertyModifiers(): Array<KModifier>

    abstract fun getPropertyInitializer(key: String): CodeBlock?
}
