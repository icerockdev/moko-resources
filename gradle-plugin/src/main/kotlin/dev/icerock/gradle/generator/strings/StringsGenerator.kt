/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.strings

import com.squareup.kotlinpoet.ClassName
import dev.icerock.gradle.generator.BaseGenerator
import org.gradle.api.file.FileTree
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

typealias LanguageType = String
typealias KeyType = String

abstract class StringsGenerator(
    protected val sourceSet: KotlinSourceSet,
    private val stringsFileTree: FileTree
) : BaseGenerator<String>() {
    override fun loadLanguageMap(): Map<LanguageType, Map<KeyType, String>> {
        return stringsFileTree.associate { file ->
            val language: LanguageType = file.parentFile.name
            val strings: Map<KeyType, String> = loadLanguageStrings(file)
            language to strings
        }
    }

    override fun getClassName(): String {
        return "strings"
    }

    override fun getPropertyClass(): ClassName {
        return ClassName("dev.icerock.moko.resources", "StringResource")
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

    override fun getImports(): List<ClassName> = emptyList()
}
