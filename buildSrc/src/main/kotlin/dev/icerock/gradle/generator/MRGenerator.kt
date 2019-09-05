/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.*
import dev.icerock.gradle.generator.strings.KeyType
import dev.icerock.gradle.generator.strings.LanguageType
import org.gradle.api.Project
import org.gradle.api.Task
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.io.File

abstract class MRGenerator(
    generatedDir: File,
    protected val sourceSet: KotlinSourceSet,
    private val mrClassPackage: String,
    private val generators: List<Generator>
) {
    private val sourcesGenerationDir = File(generatedDir, "${sourceSet.name}/src")
    protected val resourcesGenerationDir = File(generatedDir, "${sourceSet.name}/res")

    init {
        sourcesGenerationDir.mkdirs()
        sourceSet.kotlin.srcDir(sourcesGenerationDir)

        resourcesGenerationDir.mkdirs()
        sourceSet.resources.srcDir(resourcesGenerationDir)
    }

    private fun generate() {
        val mrClassSpec = TypeSpec.objectBuilder(mrClassName)
            .addModifiers(*getMRClassModifiers())

        generators.forEach { mrClassSpec.addType(it.generate(resourcesGenerationDir)) }

        processMRClass(mrClassSpec)

        val mrClass = mrClassSpec.build()

        val fileSpec = FileSpec.builder(mrClassPackage, mrClassName)
            .addType(mrClass)

        generators
            .flatMap { it.getImports() }
            .plus(getImports())
            .forEach { fileSpec.addImport(it.packageName, it.simpleName) }

        val file = fileSpec.build()
        file.writeTo(sourcesGenerationDir)
    }

    fun apply(project: Project) {
        val name = sourceSet.name
        val genTask = project.task("generateMR$name") {
            group = "multiplatform"

            doLast {
                this@MRGenerator.generate()
            }
        }

        apply(generationTask = genTask, project = project)
    }

    protected abstract fun getMRClassModifiers(): Array<KModifier>
    protected abstract fun apply(generationTask: Task, project: Project)

    protected open fun processMRClass(mrClass: TypeSpec.Builder) {}
    protected open fun getImports(): List<ClassName> = emptyList()

    private companion object {
        const val mrClassName = "MR"
    }

    interface Generator {
        fun generate(resourcesGenerationDir: File): TypeSpec
        fun getImports(): List<ClassName>
    }
}

abstract class BaseGenerator<T> : MRGenerator.Generator {
    override fun generate(resourcesGenerationDir: File): TypeSpec {
        // language - key - value
        val languageMap: Map<LanguageType, Map<KeyType, T>> = loadLanguageMap()
        val languageKeyValues = languageMap[BASE_LANGUAGE].orEmpty()

        val stringsClass = createTypeSpec(languageKeyValues.keys.toList())

        languageMap.forEach { (language, strings) ->
            if (language == BASE_LANGUAGE) {
                generateResources(resourcesGenerationDir, null, strings)
            } else {
                generateResources(resourcesGenerationDir, language, strings)
            }
        }

        return stringsClass
    }

    private fun createTypeSpec(keys: List<KeyType>): TypeSpec {
        val classBuilder = TypeSpec.objectBuilder(getClassName())
        classBuilder.addModifiers(*getClassModifiers())

        val resourceClass = getPropertyClass()

        keys.forEach { key ->
            val name = key.replace(".", "_")
            val property = PropertySpec.builder(name, resourceClass)
            property.addModifiers(*getPropertyModifiers())
            getPropertyInitializer(key)?.let { property.initializer(it) }
            classBuilder.addProperty(property.build())
        }

        return classBuilder.build()
    }

    protected abstract fun loadLanguageMap(): Map<LanguageType, Map<KeyType, T>>
    protected abstract fun getPropertyInitializer(key: String): CodeBlock?
    protected abstract fun getClassName(): String
    protected abstract fun getPropertyClass(): ClassName

    protected open fun getClassModifiers(): Array<KModifier> = emptyArray()
    protected open fun getPropertyModifiers(): Array<KModifier> = emptyArray()
    protected open fun generateResources(
        resourcesGenerationDir: File,
        language: String?,
        strings: Map<KeyType, T>
    ) {
    }

    protected companion object {
        const val BASE_LANGUAGE = "base"
    }
}
