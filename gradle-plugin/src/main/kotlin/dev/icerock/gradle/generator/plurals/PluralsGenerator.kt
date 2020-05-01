/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.plurals

import com.squareup.kotlinpoet.ClassName
import dev.icerock.gradle.generator.BaseGenerator
import dev.icerock.gradle.generator.strings.KeyType
import dev.icerock.gradle.generator.strings.LanguageType
import org.gradle.api.file.FileTree
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

typealias PluralMap = Map<String, String>

abstract class PluralsGenerator(
    private val pluralsFileTree: FileTree
) : BaseGenerator<PluralMap>() {

    override fun loadLanguageMap(): Map<LanguageType, Map<KeyType, PluralMap>> {
        return pluralsFileTree.associate { file ->
            val language: LanguageType = file.parentFile.name
            val strings: Map<KeyType, PluralMap> = loadLanguagePlurals(file)
            language to strings
        }
    }

    override fun getClassName(): String {
        return "plurals"
    }

    override fun getPropertyClass(): ClassName {
        return ClassName("dev.icerock.moko.resources", "PluralsResource")
    }

    override fun getImports(): List<ClassName> = emptyList()

    private fun loadLanguagePlurals(pluralsFile: File): Map<KeyType, PluralMap> {
        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()
        val doc = dBuilder.parse(pluralsFile)

        val pluralNodes = doc.getElementsByTagName("plural")
        val mutableMap = mutableMapOf<KeyType, PluralMap>()

        for (i in 0 until pluralNodes.length) {
            val pluralNode: Element = pluralNodes.item(i) as Element

            val pluralMap = mutableMapOf<String, String>()

            val name = pluralNode.attributes.getNamedItem("name").textContent

            val itemNodes = pluralNode.getElementsByTagName("item")
            for (j in 0 until itemNodes.length) {
                val item = itemNodes.item(j)

                val quantity = item.attributes.getNamedItem("quantity").textContent
                val value = item.textContent

                pluralMap[quantity] = value
            }

            mutableMap[name] = pluralMap
        }

        return mutableMap
    }
}
