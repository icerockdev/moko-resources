/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.resources.string

import com.squareup.kotlinpoet.PropertySpec
import dev.icerock.gradle.generator.CodeConst
import dev.icerock.gradle.generator.ResourceGenerator
import dev.icerock.gradle.generator.exceptions.EqualStringKeysException
import dev.icerock.gradle.generator.localization.LanguageType
import dev.icerock.gradle.metadata.resource.StringMetadata
import dev.icerock.gradle.utils.removeLineWraps
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

private typealias KeyType = String

internal class StringResourceGenerator(
    private val strictLineBreaks: Boolean
) : ResourceGenerator<StringMetadata> {

    override fun generateMetadata(files: Set<File>): List<StringMetadata> {
        val keyLangText: Map<KeyType, Map<LanguageType, String>> = files.flatMap { file ->
            val language: LanguageType = LanguageType.fromFileName(file.parentFile.name)
            val strings: Map<KeyType, String> = loadLanguageStrings(file)
            strings.map { (key: KeyType, text: String) ->
                key to (language to text)
            }
        }.groupBy(
            keySelector = { it.first },
            valueTransform = { it.second }
        ).mapValues { it.value.toMap() }

        return keyLangText.map { (key, langText) ->
            StringMetadata(
                key = key,
                values = langText.map { (lang, value) ->
                    StringMetadata.LocaleItem(
                        locale = lang.language(),
                        value = value
                    )
                }
            )
        }
    }

    override fun generateProperty(metadata: StringMetadata): PropertySpec.Builder {
        return PropertySpec.builder(metadata.key, CodeConst.stringResourceName)
    }

    private fun loadLanguageStrings(stringsFile: File): Map<KeyType, String> {
        val dbFactory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
        val dBuilder: DocumentBuilder = dbFactory.newDocumentBuilder()
        val doc: Document = dBuilder.parse(stringsFile)

        val stringNodes: NodeList = doc.getElementsByTagName("string")

        val resultMap: MutableMap<KeyType, String> = mutableMapOf()

        for (i: Int in 0 until stringNodes.length) {
            val stringNode: Node = stringNodes.item(i)
            val name: String = stringNode.attributes.getNamedItem("name").textContent
            val value: String = stringNode.textContent

            resultMap[name] = if (strictLineBreaks) value else value.removeLineWraps()
        }

        // check keys == values for apple locale change logic
        // TODO maybe remove this logic?
        val incorrectKeys: List<KeyType> = resultMap
            .filter { it.key == it.value }
            .keys
            .toList()

        if (incorrectKeys.isNotEmpty()) {
            throw EqualStringKeysException(incorrectKeys)
        }

        return resultMap
    }
}
