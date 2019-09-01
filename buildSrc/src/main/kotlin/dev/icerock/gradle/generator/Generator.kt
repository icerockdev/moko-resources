/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.*
import org.gradle.api.Project
import org.gradle.api.Task
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.io.File

typealias LanguageType = String
typealias KeyType = String

abstract class Generator(
    protected val generatedDir: File,
    protected val sourceSet: KotlinSourceSet,
    protected val languagesStrings: Map<LanguageType, Map<KeyType, String>>,
    protected val mrClassPackage: String
) {
    private val sourcesGenerationDir = File(generatedDir, "${sourceSet.name}/src")

    init {
        sourceSet.kotlin.srcDir(sourcesGenerationDir)
    }

    fun generate() {
        generateMRFile()

        languagesStrings.forEach { (language, strings) ->
            if (language == BASE_LANGUAGE) {
                generateResources(null, strings)
            } else {
                generateResources(language, strings)
            }
        }
    }

    private fun generateMRFile() {
        val baseStrings = languagesStrings[BASE_LANGUAGE].orEmpty()

        val stringsClass = generateStrings(baseStrings)

        val mrClass = TypeSpec.objectBuilder("MR")
            .addModifiers(*getMRClassModifiers())
            .addType(stringsClass)
            .apply { classMRAdditions(this) }
            .build()

        val file = FileSpec.builder(mrClassPackage, "MR")
            .addType(mrClass)
            .apply { getImports().forEach { addImport(it.packageName, it.simpleName) } }
            .build()

        file.writeTo(sourcesGenerationDir)
    }

    private fun generateStrings(strings: Map<KeyType, String>): TypeSpec {
        val classBuilder = TypeSpec.objectBuilder("strings")
        classBuilder.addModifiers(*getStringsClassModifiers())

        val stringResourceClass = ClassName("dev.icerock.moko.resources", "StringResource")

        strings.forEach { (key, _) ->
            val name = key.replace(".", "_")
            val property = PropertySpec.builder(name, stringResourceClass)
            property.addModifiers(*getStringsPropertyModifiers())
            getStringsPropertyInitializer(key)?.let { initializer ->
                property.initializer(initializer)
            }
            classBuilder.addProperty(property.build())
        }

        return classBuilder.build()
    }

    protected open fun getMRClassModifiers(): Array<KModifier> = emptyArray()
    protected open fun getStringsClassModifiers(): Array<KModifier> = emptyArray()
    protected open fun getStringsPropertyModifiers(): Array<KModifier> = emptyArray()
    protected open fun getImports(): Array<ClassName> = emptyArray()
    protected open fun generateResources(language: String?, strings: Map<KeyType, String>) {}
    protected open fun classMRAdditions(classSpec: TypeSpec.Builder) {}

    protected abstract fun getStringsPropertyInitializer(key: String): CodeBlock?
    abstract fun configureTasks(generationTask: Task, project: Project)

    open fun postBuildActions(project: Project, task: Task) {}

    protected companion object {
        const val BASE_LANGUAGE = "base"
    }
}

