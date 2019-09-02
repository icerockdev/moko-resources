/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.strings

import com.squareup.kotlinpoet.*
import dev.icerock.gradle.generator.MRGenerator
import org.gradle.api.file.FileTree
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

typealias LanguageType = String
typealias KeyType = String

abstract class StringsGenerator(
    protected val sourceSet: KotlinSourceSet,
    private val stringsFileTree: FileTree
) : MRGenerator.Generator {
    override fun generate(resourcesGenerationDir: File): TypeSpec {
        // language - key - value
        val languagesStrings: Map<LanguageType, Map<KeyType, String>> = loadStrings(stringsFileTree)

        val baseStrings = languagesStrings[BASE_LANGUAGE].orEmpty()

        val stringsClass = generateStrings(baseStrings)

        languagesStrings.forEach { (language, strings) ->
            if (language == BASE_LANGUAGE) {
                generateResources(resourcesGenerationDir, null, strings)
            } else {
                generateResources(resourcesGenerationDir, language, strings)
            }
        }

        return stringsClass
    }

    private fun loadStrings(stringsFileTree: FileTree): Map<LanguageType, Map<KeyType, String>> {
        return stringsFileTree.associate { file ->
            val language: LanguageType = file.parentFile.name
            val strings: Map<KeyType, String> = loadLanguageStrings(file)
            language to strings
        }
    }

    private fun loadLanguageStrings(stringsFile: File): Map<KeyType, String> {
        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()
        val doc = dBuilder.parse(stringsFile)

        val stringNodes = doc.getElementsByTagName("string")
        val mutableMap = mutableMapOf<KeyType, String>()

        for (i in 0 until stringNodes.length) {
            val stringNode = stringNodes.item(i)
            val name = stringNode.attributes.getNamedItem("name").textContent
            val value = stringNode.textContent

            mutableMap[name] = value
        }

        return mutableMap
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

    protected open fun getStringsClassModifiers(): Array<KModifier> = emptyArray()
    protected open fun getStringsPropertyModifiers(): Array<KModifier> = emptyArray()
    protected open fun generateResources(
        resourcesGenerationDir: File,
        language: String?,
        strings: Map<KeyType, String>
    ) {
    }

    protected abstract fun getStringsPropertyInitializer(key: String): CodeBlock?

    override fun getImports(): List<ClassName> = emptyList()

    protected companion object {
        const val BASE_LANGUAGE = "base"
    }
}
