/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle

import com.squareup.kotlinpoet.*
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.io.File

typealias LanguageType = String
typealias KeyType = String

sealed class Generator(
    protected val generatedDir: File,
    protected val sourceSet: KotlinSourceSet,
    protected val languagesStrings: Map<LanguageType, Map<KeyType, String>>,
    protected val mrClassPackage: String
) {
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
        val name = sourceSet.name
        val generationDir = File(generatedDir, "$name/src")

        sourceSet.kotlin.srcDir(generationDir)

        val baseStrings = languagesStrings[BASE_LANGUAGE].orEmpty()

        val stringsClass = generateStrings(baseStrings)

        val mrClass = TypeSpec.objectBuilder("MR")
            .addModifiers(*getMRClassModifiers())
            .addType(stringsClass)
            .build()

        val file = FileSpec.builder(mrClassPackage, "MR")
            .addType(mrClass)
            .apply { getImports().forEach { addImport(it.packageName, it.simpleName) } }
            .build()

        file.writeTo(generationDir)
    }

    private fun generateStrings(strings: Map<KeyType, String>): TypeSpec {
        val classBuilder = TypeSpec.objectBuilder("strings")
        classBuilder.addModifiers(*getStringsClassModifiers())

        val stringResourceClass = ClassName("dev.icerock.moko.resources", "StringResource")

        strings.forEach { (key, _) ->
            val property = PropertySpec.builder(key, stringResourceClass)
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

    protected abstract fun getStringsPropertyInitializer(key: String): CodeBlock?

    class Common(
        generatedDir: File,
        sourceSet: KotlinSourceSet,
        languagesStrings: Map<LanguageType, Map<KeyType, String>>,
        mrClassPackage: String
    ) : Generator(
        generatedDir = generatedDir,
        sourceSet = sourceSet,
        languagesStrings = languagesStrings,
        mrClassPackage = mrClassPackage
    ) {
        override fun getMRClassModifiers(): Array<KModifier> = arrayOf(KModifier.EXPECT)

        override fun getStringsPropertyInitializer(key: String): CodeBlock? = null
    }

    class Android(
        generatedDir: File,
        sourceSet: KotlinSourceSet,
        languagesStrings: Map<LanguageType, Map<KeyType, String>>,
        mrClassPackage: String,
        private val androidRClassPackage: String
    ) : Generator(
        generatedDir = generatedDir,
        sourceSet = sourceSet,
        languagesStrings = languagesStrings,
        mrClassPackage = mrClassPackage
    ) {
        override fun getMRClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

        override fun getStringsClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

        override fun getStringsPropertyModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

        override fun getStringsPropertyInitializer(key: String): CodeBlock? =
            CodeBlock.of("StringResource(R.string.%L)", key)

        override fun getImports(): Array<ClassName> = arrayOf(
            ClassName(androidRClassPackage, "R")
        )

        override fun generateResources(language: String?, strings: Map<KeyType, String>) {
            val name = sourceSet.name
            val generationDir = File(generatedDir, "$name/res")

            sourceSet.resources.srcDir(generationDir)

            val valuesDir = File(generationDir, "values")
            val stringsFile = File(valuesDir, "mr_strings.xml")
            valuesDir.mkdirs()

            val header = """
<?xml version="1.0" encoding="utf-8"?>
<resources>
            """.trimIndent()

            val content = strings.map { (key, value) ->
                "\t<string name=\"$key\">$value</string>"
            }.joinToString("\n")

            val footer = """
</resources>
            """.trimIndent()

            stringsFile.writeText(header + "\n")
            stringsFile.appendText(content)
            stringsFile.appendText("\n" + footer)
        }
    }

    class iOS(
        generatedDir: File,
        sourceSet: KotlinSourceSet,
        languagesStrings: Map<LanguageType, Map<KeyType, String>>,
        mrClassPackage: String
    ) : Generator(
        generatedDir = generatedDir,
        sourceSet = sourceSet,
        languagesStrings = languagesStrings,
        mrClassPackage = mrClassPackage
    ) {
        override fun getMRClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

        override fun getStringsClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

        override fun getStringsPropertyModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

        override fun getStringsPropertyInitializer(key: String): CodeBlock? =
            CodeBlock.of("StringResource(%S)", key)

        override fun generateResources(language: String?, strings: Map<KeyType, String>) {
            super.generateResources(language, strings)
        }
    }

    protected companion object {
        const val BASE_LANGUAGE = "base"
    }
}

