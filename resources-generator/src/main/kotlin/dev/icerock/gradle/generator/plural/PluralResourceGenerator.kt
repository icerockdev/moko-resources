/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.plural

import com.squareup.kotlinpoet.PropertySpec
import dev.icerock.gradle.generator.CodeConst
import dev.icerock.gradle.generator.ResourceGenerator
import dev.icerock.gradle.generator.localization.LanguageType
import dev.icerock.gradle.metadata.resource.PluralMetadata
import dev.icerock.gradle.utils.removeLineWraps
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

private typealias KeyType = String
private typealias PluralMap = Map<String, String>

internal class PluralResourceGenerator(
    private val strictLineBreaks: Boolean
) : ResourceGenerator<PluralMetadata> {

    override fun generateMetadata(files: Set<File>): List<PluralMetadata> {
        val keyLangText: Map<KeyType, Map<LanguageType, PluralMap>> = files.flatMap { file ->
            val language: LanguageType = LanguageType.fromFileName(file.parentFile.name)
            val strings: Map<KeyType, PluralMap> = loadLanguageStrings(file)
            strings.map { (key: KeyType, plural: PluralMap) ->
                key to (language to plural)
            }
        }.groupBy(
            keySelector = { it.first },
            valueTransform = { it.second }
        ).mapValues { it.value.toMap() }

        return keyLangText.map { (key: KeyType, langText: Map<LanguageType, PluralMap>) ->
            PluralMetadata(
                key = key,
                values = langText.map { (lang: LanguageType, value: PluralMap) ->
                    PluralMetadata.LocaleItem(
                        locale = lang.language(),
                        values = value.map { (quantity: String, value: String) ->
                            PluralMetadata.PluralItem(
                                quantity = PluralMetadata.PluralItem.Quantity.valueOf(quantity.uppercase()),
                                value = value
                            )
                        }
                    )
                }
            )
        }
    }

    override fun generateProperty(metadata: PluralMetadata): PropertySpec.Builder {
        return PropertySpec.builder(metadata.key, CodeConst.pluralsResourceName)
    }

    private fun loadLanguageStrings(stringsFile: File): Map<KeyType, PluralMap> {
        val dbFactory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
        val dBuilder: DocumentBuilder = dbFactory.newDocumentBuilder()
        val doc: Document = dBuilder.parse(stringsFile)

        val resultMap: MutableMap<KeyType, PluralMap> = mutableMapOf()

        doc.findPluralNodes().forEach { pluralNode ->
            val pluralMap: MutableMap<String, String> = mutableMapOf()

            val name: String = pluralNode.attributes.getNamedItem("name").textContent
            val itemNodes: NodeList = pluralNode.getElementsByTagName("item")
            for (j: Int in 0 until itemNodes.length) {
                val item: Node = itemNodes.item(j)

                val quantity: String = item.attributes.getNamedItem("quantity").textContent.trim()
                val value: String = item.textContent

                pluralMap[quantity] = if (strictLineBreaks) value else value.removeLineWraps()
            }

            resultMap[name] = pluralMap
        }

        return resultMap
    }

    private fun Document.findPluralNodes(): Sequence<Element> = sequence {
        SOURCE_PLURAL_NODE_NAMES.forEach { elementName ->
            val pluralNodes: NodeList = getElementsByTagName(elementName)
            for (i: Int in 0 until pluralNodes.length) {
                yield(pluralNodes.item(i) as Element)
            }
        }
    }

    private companion object {
        val SOURCE_PLURAL_NODE_NAMES: List<String> = listOf("plural", "plurals")
    }
}
