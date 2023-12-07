/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.PropertySpec.Builder
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.generator.android.AndroidStringsGenerator
import dev.icerock.gradle.generator.apple.AppleStringsGenerator
import dev.icerock.gradle.generator.common.CommonStringsGenerator
import dev.icerock.gradle.generator.js.JsStringsGenerator
import dev.icerock.gradle.generator.jvm.JvmStringsGenerator
import dev.icerock.gradle.metadata.GeneratedObject
import dev.icerock.gradle.metadata.GeneratedObjectModifier
import dev.icerock.gradle.metadata.GeneratedObjectType
import dev.icerock.gradle.metadata.GeneratedProperties
import dev.icerock.gradle.metadata.GeneratorType
import dev.icerock.gradle.metadata.addActual
import dev.icerock.gradle.metadata.getActualInterfaces
import dev.icerock.gradle.metadata.objectsWithProperties
import dev.icerock.gradle.utils.removeLineWraps
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import org.gradle.api.Project
import org.gradle.api.file.FileTree

typealias KeyType = String

abstract class StringsGenerator(
    private val ownStringsFileTree: FileTree,
    private val strictLineBreaks: Boolean,
) : BaseGenerator<String>() {

    override fun generate(
        project: Project,
        inputMetadata: MutableList<GeneratedObject>,
        generatedObjects: MutableList<GeneratedObject>,
        targetObject: GeneratedObject,
        assetsGenerationDir: File,
        resourcesGenerationDir: File,
        objectBuilder: TypeSpec.Builder,
    ): TypeSpec {

        val previousLanguagesMap: Map<LanguageType, Map<KeyType, String>> = getPreviousLanguagesMap(
            inputMetadata = inputMetadata,
            targetObject = targetObject
        )

        // language - key - value
        val languageMap: Map<LanguageType, Map<KeyType, String>> = if (targetObject.isActualObject) {
            emptyMap()
        } else {
            loadLanguageMap()
        }

        val languagesAllMaps = getLanguagesAllMaps(previousLanguagesMap, languageMap)
        val languageKeyValues = languagesAllMaps[LanguageType.Base].orEmpty()

        beforeGenerateResources(objectBuilder, languagesAllMaps)

        val stringsClass = createTypeSpec(
            inputMetadata = inputMetadata,
            generatedObjects = generatedObjects,
            targetObject = targetObject,
            keys = languageKeyValues.keys.toList(),
            languageMap = languagesAllMaps,
            objectBuilder = objectBuilder
        )

        languagesAllMaps.forEach { (language: LanguageType, strings: Map<KeyType, String>) ->
            generateResources(resourcesGenerationDir, language, strings)
        }

        return stringsClass
    }

    private fun getLanguagesAllMaps(
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

    private fun getPreviousLanguagesMap(
        inputMetadata: List<GeneratedObject>,
        targetObject: GeneratedObject,
    ): Map<LanguageType, Map<KeyType, String>> {
        if (!targetObject.isObject || !targetObject.isActual) return emptyMap()

        val objectsWithProperties: List<GeneratedObject> = inputMetadata.objectsWithProperties(targetObject)

        val languagesMaps = mutableMapOf<LanguageType, Map<KeyType, String>>()

        objectsWithProperties.forEach { generatedObject ->
            generatedObject.properties.forEach { property ->
                val data = Json.decodeFromJsonElement<Map<String, JsonPrimitive>>(property.data)

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

    @Suppress("SpreadOperator")
    private fun createTypeSpec(
        inputMetadata: MutableList<GeneratedObject>,
        generatedObjects: MutableList<GeneratedObject>,
        targetObject: GeneratedObject,
        keys: List<KeyType>,
        languageMap: Map<LanguageType, Map<KeyType, String>>,
        objectBuilder: TypeSpec.Builder,
    ): TypeSpec {
        objectBuilder.addModifiers(*getClassModifiers())

        extendObjectBodyAtStart(objectBuilder)

        val generatedProperties = mutableListOf<GeneratedProperties>()

        keys.forEach { key ->
            val name = key.replace(".", "_")

            val values = mutableMapOf<String, JsonPrimitive>()

            languageMap.forEach { (languageType, strings) ->
                strings.forEach { (stringKey, value) ->
                    if (stringKey == key) {
                        values[languageType.language()] = JsonPrimitive(value)
                    }
                }
            }

            var generatedProperty = GeneratedProperties(
                modifier = GeneratedObjectModifier.None,
                name = name,
                data = JsonObject(values)
            )

            val property: Builder = PropertySpec.builder(name, resourceClassName)

            if (targetObject.isObject) {
                // Add modifier for property and setup metadata
                generatedProperty = generatedProperty.copy(
                    modifier = addActualOverrideModifier(
                        propertyName = name,
                        property = property,
                        inputMetadata = inputMetadata,
                        targetObject = targetObject
                    )
                )

                getPropertyInitializer(key)?.let {
                    property.initializer(it)
                }
            }

            objectBuilder.addProperty(property.build())

            generatedProperties.add(generatedProperty)
        }

        extendObjectBodyAtEnd(objectBuilder)

        generatedObjects.addActual(
            targetObject.copy(properties = generatedProperties)
        )

        return objectBuilder.build()
    }

    private fun addActualOverrideModifier(
        propertyName: String,
        property: PropertySpec.Builder,
        inputMetadata: List<GeneratedObject>,
        targetObject: GeneratedObject,
    ): GeneratedObjectModifier {
        val actualInterfaces = inputMetadata.getActualInterfaces(
            generatorType = targetObject.generatorType
        )

        var containsInActualInterfaces = false

        actualInterfaces.forEach { genInterface ->
            val hasInInterface = genInterface.properties.any {
                it.name == propertyName
            }

            if (hasInInterface) {
                containsInActualInterfaces = true
            }
        }

        return if (targetObject.type == GeneratedObjectType.Object) {
            if (containsInActualInterfaces) {
                property.addModifiers(KModifier.OVERRIDE)
                GeneratedObjectModifier.Override
            } else {
                when (targetObject.modifier) {
                    GeneratedObjectModifier.Expect -> {
                        property.addModifiers(KModifier.EXPECT)
                        GeneratedObjectModifier.Expect
                    }

                    GeneratedObjectModifier.Actual -> {
                        property.addModifiers(KModifier.ACTUAL)
                        GeneratedObjectModifier.Actual
                    }

                    else -> {
                        GeneratedObjectModifier.None
                    }
                }
            }
        } else {
            GeneratedObjectModifier.None
        }
    }

    override val inputFiles: Iterable<File>
        get() = (ownStringsFileTree).matching { it.include(STRINGS_MASK) }.files

    override val resourceClassName = ClassName("dev.icerock.moko.resources", "StringResource")
    override val mrObjectName: String = "strings"

    override val type: GeneratorType = GeneratorType.Strings

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
            ownStringsFileTree = settings.ownResourcesFileTree,
            strictLineBreaks = settings.isStrictLineBreaks
        )

        override fun createIosGenerator(): StringsGenerator = AppleStringsGenerator(
            ownStringsFileTree = settings.ownResourcesFileTree,
            strictLineBreaks = settings.isStrictLineBreaks,
            baseLocalizationRegion = settings.iosLocalizationRegion
        )

        override fun createAndroidGenerator(): StringsGenerator = AndroidStringsGenerator(
            ownStringsFileTree = settings.ownResourcesFileTree,
            strictLineBreaks = settings.isStrictLineBreaks,
            androidRClassPackage = settings.androidRClassPackage
        )

        override fun createJsGenerator(): StringsGenerator = JsStringsGenerator(
            ownStringsFileTree = settings.ownResourcesFileTree,
            mrClassPackage = settings.packageName,
            strictLineBreaks = settings.isStrictLineBreaks
        )

        override fun createJvmGenerator(): StringsGenerator = JvmStringsGenerator(
            ownStringsFileTree = settings.ownResourcesFileTree,
            strictLineBreaks = settings.isStrictLineBreaks,
            settings = settings
        )
    }

    companion object {
        const val STRINGS_MASK = "**/strings*.xml"
    }
}
