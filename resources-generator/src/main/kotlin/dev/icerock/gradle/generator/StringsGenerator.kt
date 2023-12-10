/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.ClassName
import dev.icerock.gradle.generator.android.AndroidStringsGenerator
import dev.icerock.gradle.generator.apple.AppleStringsGenerator
import dev.icerock.gradle.generator.common.CommonStringsGenerator
import dev.icerock.gradle.generator.js.JsStringsGenerator
import dev.icerock.gradle.generator.jvm.JvmStringsGenerator
import dev.icerock.gradle.metadata.GeneratedObject
import dev.icerock.gradle.metadata.GeneratorType
import dev.icerock.gradle.metadata.objectsWithProperties
import dev.icerock.gradle.utils.removeLineWraps
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import org.gradle.api.file.FileTree
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

typealias KeyType = String

abstract class StringsGenerator(
    private val resourcesFileTree: FileTree,
    private val strictLineBreaks: Boolean,
) : BaseGenerator<String>() {

    override val inputFiles: Iterable<File>
        get() = resourcesFileTree.matching { it.include(STRINGS_MASK) }.files

    override val resourceClassName = ClassName("dev.icerock.moko.resources", "StringResource")
    override val mrObjectName: String = "strings"

    override val type: GeneratorType = GeneratorType.Strings

    override fun getLanguagesAllMaps(
        previousLanguageMaps: Map<LanguageType, Map<KeyType, String>>,
        languageMap: Map<LanguageType, Map<KeyType, String>>
    ): Map<LanguageType, Map<KeyType, String>> {
        val resultLanguageMap: MutableMap<LanguageType, Map<KeyType, String>> = mutableMapOf()

        resultLanguageMap.putAll(previousLanguageMaps)

        languageMap.forEach { (languageType: LanguageType, value: Map<KeyType, String>) ->

            val currentMap: MutableMap<KeyType, String> =
                previousLanguageMaps[languageType]?.toMutableMap() ?: mutableMapOf()

            value.forEach { (key, value) -> currentMap[key] = value }

            resultLanguageMap[languageType] = currentMap
        }

        return resultLanguageMap
    }

    override fun getPreviousLanguagesMap(
        inputMetadata: List<GeneratedObject>,
        targetObject: GeneratedObject,
    ): Map<LanguageType, Map<KeyType, String>> {
        if (!targetObject.isObject || !targetObject.isActual) return emptyMap()
        val json = Json
        val objectsWithProperties: List<GeneratedObject> = inputMetadata.objectsWithProperties(targetObject)

        val languagesMaps = mutableMapOf<LanguageType, Map<KeyType, String>>()

        objectsWithProperties.forEach { generatedObject ->
            generatedObject.properties.forEach { property ->
                val data = json.decodeFromJsonElement<Map<String, JsonPrimitive>>(property.data)

                data.forEach { (languageTag, value) ->
                    val languageType: LanguageType = if (languageTag == BASE_LANGUAGE) {
                        LanguageType.Base
                    } else {
                        LanguageType.Locale(languageTag)
                    }

                    val currentMap: MutableMap<KeyType, String> =
                        languagesMaps[languageType]?.toMutableMap() ?: mutableMapOf()

                    currentMap[property.name] = value.content

                    languagesMaps[languageType] = currentMap
                }
            }
        }

        return languagesMaps
    }

    override fun getPropertyMetadata(
        key: KeyType,
        languageMap: Map<LanguageType, Map<KeyType, String>>,
    ): Map<String, JsonElement> {
        val values = mutableMapOf<String, JsonElement>()

        languageMap.forEach { (languageType, strings) ->
            strings.forEach { (stringKey, value) ->
                if (stringKey == key) {
                    values[languageType.language()] = JsonPrimitive(value)
                }
            }
        }

        return values
    }


    override fun loadLanguageMap(): Map<LanguageType, Map<KeyType, String>> {
        return inputFiles.map { file ->
            val language: LanguageType = LanguageType.fromFileName(file.parentFile.name)
            val strings: Map<KeyType, String> = loadLanguageStrings(file)
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

            mutableMap[name] = if (strictLineBreaks) value else value.removeLineWraps()
        }

        val incorrectKeys = mutableMap
            .filter { it.key == it.value }
            .keys
            .toList()

        if (incorrectKeys.isNotEmpty()) {
            throw EqualStringKeysException(incorrectKeys)
        }

        return mutableMap
    }

    override fun getImports(): List<ClassName> = emptyList()

    class Feature(
        private val settings: MRGenerator.Settings,
    ) : ResourceGeneratorFeature<StringsGenerator> {
        override fun createCommonGenerator(): StringsGenerator = CommonStringsGenerator(
            resourcesFileTree = settings.ownResourcesFileTree,
            strictLineBreaks = settings.isStrictLineBreaks
        )

        override fun createIosGenerator(): StringsGenerator = AppleStringsGenerator(
            resourcesFileTree = settings.ownResourcesFileTree,
            strictLineBreaks = settings.isStrictLineBreaks,
            baseLocalizationRegion = settings.iosLocalizationRegion
        )

        override fun createAndroidGenerator(): StringsGenerator = AndroidStringsGenerator(
            resourcesFileTree = settings.ownResourcesFileTree,
            strictLineBreaks = settings.isStrictLineBreaks,
            androidRClassPackage = settings.androidRClassPackage
        )

        override fun createJsGenerator(): StringsGenerator = JsStringsGenerator(
            resourcesFileTree = settings.ownResourcesFileTree,
            mrClassPackage = settings.packageName,
            strictLineBreaks = settings.isStrictLineBreaks
        )

        override fun createJvmGenerator(): StringsGenerator = JvmStringsGenerator(
            resourcesFileTree = settings.ownResourcesFileTree,
            strictLineBreaks = settings.isStrictLineBreaks,
            settings = settings
        )
    }

    companion object {
        const val STRINGS_MASK = "**/strings*.xml"
    }
}
