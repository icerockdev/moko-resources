/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.ClassName
import dev.icerock.gradle.generator.android.AndroidPluralsGenerator
import dev.icerock.gradle.generator.common.CommonPluralsGenerator
import dev.icerock.gradle.generator.apple.ApplePluralsGenerator
import dev.icerock.gradle.generator.js.JsPluralsGenerator
import dev.icerock.gradle.generator.jvm.JvmPluralsGenerator
import org.gradle.api.file.FileTree
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

typealias PluralMap = Map<String, String>

/**
 * How the plural element can be declared in our xml files.
 *
 * 'plurals' match the element name used on the Android platform. This allows us to upload the source xml as-is to
 * translation websites as they'll interpret it as an Android resource file.
 */
private val SOURCE_PLURAL_NODE_NAMES = listOf("plural", "plurals")

abstract class PluralsGenerator(
    private val pluralsFileTree: FileTree
) : BaseGenerator<PluralMap>() {

    override val inputFiles: Iterable<File> get() = pluralsFileTree.files
    override val resourceClassName = ClassName("dev.icerock.moko.resources", "PluralsResource")
    override val mrObjectName: String = "plurals"

    override fun loadLanguageMap(): Map<LanguageType, Map<KeyType, PluralMap>> {
        return pluralsFileTree.map { file ->
            val language: LanguageType = file.parentFile.name
            val strings: Map<KeyType, PluralMap> = loadLanguagePlurals(file)
            language to strings
        }.groupBy(
            keySelector = { it.first },
            valueTransform = { it.second }
        ).mapValues { value ->
            val maps = value.value
            maps.fold(mutableMapOf()) { result, keyValueMap ->
                result.putAll(keyValueMap)
                result
            }
        }
    }

    override fun getImports(): List<ClassName> = emptyList()

    private fun loadLanguagePlurals(pluralsFile: File): Map<KeyType, PluralMap> {
        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()
        val doc = dBuilder.parse(pluralsFile)

        val mutableMap = mutableMapOf<KeyType, PluralMap>()

        doc.findPluralNodes().forEach { pluralNode ->
            val pluralMap = mutableMapOf<String, String>()

            val name = pluralNode.attributes.getNamedItem("name").textContent

            val itemNodes = pluralNode.getElementsByTagName("item")
            for (j in 0 until itemNodes.length) {
                val item = itemNodes.item(j)

                val quantity = item.attributes.getNamedItem("quantity").textContent.trim()
                val value = item.textContent

                pluralMap[quantity] = value
            }

            mutableMap[name] = pluralMap
        }

        return processLanguagePlurals(mutableMap)
    }

    /**
     * Called after the language plurals have been loaded.
     */
    protected open fun processLanguagePlurals(languagePlurals: Map<KeyType, PluralMap>): Map<KeyType, PluralMap> {
        // Replace =0 with zero. =1 with one and =2 and two
        // if both =0 and zero are present, =0 will be discarded. Same with =1 and one and =2 with two

        val manipulateMap: MutableMap<String, String>.(equals: String, literal: String) -> Unit = { equals, literal ->
            val hasEquals = equals in this
            val hasLiteral = literal in this

            if (hasEquals && hasLiteral) {
                this -= equals
            } else if (hasEquals) {
                this[literal] = this.remove(equals)!!
            }
        }

        return languagePlurals.map { (keyType, pluralMap) ->
            val newPluralMap = pluralMap.toMutableMap()

            newPluralMap.manipulateMap("=0", "zero")
            newPluralMap.manipulateMap("=1", "one")
            newPluralMap.manipulateMap("=2", "two")

            keyType to newPluralMap
        }.toMap()
    }

    private fun Document.findPluralNodes() = sequence {
        SOURCE_PLURAL_NODE_NAMES.forEach { elementName ->
            val pluralNodes = getElementsByTagName(elementName)
            for (i in 0 until pluralNodes.length) {
                yield(pluralNodes.item(i) as Element)
            }
        }
    }

    class Feature(
        private val info: SourceInfo,
        private val iosBaseLocalizationRegion: String,
        private val mrSettings: MRGenerator.MRSettings
    ) : ResourceGeneratorFeature<PluralsGenerator> {
        private val stringsFileTree =
            info.commonResources.matching { it.include("MR/**/plurals*.xml") }

        override fun createCommonGenerator(): PluralsGenerator {
            return CommonPluralsGenerator(stringsFileTree)
        }

        override fun createIosGenerator(): PluralsGenerator {
            return ApplePluralsGenerator(
                stringsFileTree,
                iosBaseLocalizationRegion
            )
        }

        override fun createAndroidGenerator(): PluralsGenerator {
            return AndroidPluralsGenerator(
                stringsFileTree,
                info.androidRClassPackage
            )
        }

        override fun createJsGenerator(): PluralsGenerator = JsPluralsGenerator(
            stringsFileTree,
            mrSettings.packageName
        )

        override fun createJvmGenerator() = JvmPluralsGenerator(
            stringsFileTree,
            mrSettings
        )
    }
}
