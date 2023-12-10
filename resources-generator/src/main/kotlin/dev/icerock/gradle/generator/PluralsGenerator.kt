/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.ClassName
import dev.icerock.gradle.generator.android.AndroidPluralsGenerator
import dev.icerock.gradle.generator.apple.ApplePluralsGenerator
import dev.icerock.gradle.generator.common.CommonPluralsGenerator
import dev.icerock.gradle.generator.js.JsPluralsGenerator
import dev.icerock.gradle.generator.jvm.JvmPluralsGenerator
import dev.icerock.gradle.metadata.GeneratedObject
import dev.icerock.gradle.metadata.GeneratorType
import dev.icerock.gradle.metadata.objectsWithProperties
import dev.icerock.gradle.utils.removeLineWraps
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
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
    private val ownResourcesFileTree: FileTree,
    private val strictLineBreaks: Boolean
) : BaseGenerator<PluralMap>() {

    override val inputFiles: Iterable<File>
        get() = ownResourcesFileTree.matching { it.include(PLURALS_MASK) }.files

    override val resourceClassName = ClassName("dev.icerock.moko.resources", "PluralsResource")
    override val mrObjectName: String = "plurals"

    override val type: GeneratorType = GeneratorType.Plurals

    override fun getLanguagesAllMaps(
        previousLanguageMaps: Map<LanguageType, Map<KeyType, PluralMap>>,
        languageMap: Map<LanguageType, Map<KeyType, PluralMap>>
    ): Map<LanguageType, Map<KeyType, PluralMap>> {
        val resultLanguageMap: MutableMap<LanguageType, Map<KeyType, PluralMap>> = mutableMapOf()

        resultLanguageMap.putAll(previousLanguageMaps)

        languageMap.forEach { (languageType: LanguageType, value: Map<KeyType, PluralMap>) ->

            val currentMap: MutableMap<KeyType, PluralMap> =
                previousLanguageMaps[languageType]?.toMutableMap() ?: mutableMapOf()

            value.forEach { (key, value) -> currentMap[key] = value }

            resultLanguageMap[languageType] = currentMap
        }

        return resultLanguageMap
    }

    override fun getPreviousLanguagesMap(
        inputMetadata: List<GeneratedObject>,
        targetObject: GeneratedObject,
    ): Map<LanguageType, Map<KeyType, PluralMap>> {
        if (!targetObject.isObject || !targetObject.isActual) return emptyMap()
        val json = Json
        val objectsWithProperties: List<GeneratedObject> = inputMetadata.objectsWithProperties(targetObject)

        val languagesMaps = mutableMapOf<LanguageType, Map<KeyType, PluralMap>>()

        objectsWithProperties.forEach { generatedObject ->
            generatedObject.properties.forEach { property ->
                val data = json.decodeFromJsonElement<Map<String, PluralMap>>(property.data)

                data.forEach { (languageTag, value) ->
                    val languageType: LanguageType = if (languageTag == BASE_LANGUAGE) {
                        LanguageType.Base
                    } else {
                        LanguageType.Locale(languageTag)
                    }

                    val currentMap: MutableMap<KeyType, PluralMap> =
                        languagesMaps[languageType]?.toMutableMap() ?: mutableMapOf()
                    currentMap[property.name] = value

                    languagesMaps[languageType] = currentMap
                }
            }
        }

        return languagesMaps
    }

    override fun getPropertyMetadata(
        key: KeyType,
        languageMap: Map<LanguageType, Map<KeyType, PluralMap>>
    ): Map<String, JsonElement> {
        val values = mutableMapOf<String, JsonElement>()

        languageMap.forEach { (languageType, plurals) ->
            plurals.forEach { (pluralKey: KeyType, value: Map<String, String>) ->
                if (pluralKey == key) {
                    values[languageType.language()] = JsonObject(content = value.mapValues {
                        JsonPrimitive(it.value)
                    })
                }
            }
        }

        return values
    }

    override fun loadLanguageMap(): Map<LanguageType, Map<KeyType, PluralMap>> {
        return inputFiles.map { file ->
            val language: LanguageType = LanguageType.fromFileName(file.parentFile.name)
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

                pluralMap[quantity] = if (strictLineBreaks) value else value.removeLineWraps()
            }

            mutableMap[name] = pluralMap
        }

        return mutableMap
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
        private val settings: MRGenerator.Settings
    ) : ResourceGeneratorFeature<PluralsGenerator> {
        override fun createCommonGenerator(): PluralsGenerator = CommonPluralsGenerator(
            ownResourcesFileTree = settings.ownResourcesFileTree,
            strictLineBreaks = settings.isStrictLineBreaks
        )

        override fun createIosGenerator(): PluralsGenerator = ApplePluralsGenerator(
            ownResourcesFileTree = settings.ownResourcesFileTree,
            strictLineBreaks = settings.isStrictLineBreaks,
            baseLocalizationRegion = settings.iosLocalizationRegion
        )

        override fun createAndroidGenerator(): PluralsGenerator = AndroidPluralsGenerator(
            ownResourcesFileTree = settings.ownResourcesFileTree,
            strictLineBreaks = settings.isStrictLineBreaks,
            androidRClassPackage = settings.androidRClassPackage,
        )

        override fun createJvmGenerator(): PluralsGenerator = JvmPluralsGenerator(
            ownResourcesFileTree = settings.ownResourcesFileTree,
            strictLineBreaks = settings.isStrictLineBreaks,
            settings = settings
        )

        override fun createJsGenerator(): PluralsGenerator = JsPluralsGenerator(
            ownResourcesFileTree = settings.ownResourcesFileTree,
            mrClassPackage = settings.packageName,
            strictLineBreaks = settings.isStrictLineBreaks
        )
    }


    companion object {
        const val PLURALS_MASK = "**/plurals*.xml"
    }
}
