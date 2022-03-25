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
import dev.icerock.gradle.utils.removeLineWraps
import org.gradle.api.file.FileTree
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

typealias LanguageType = String
typealias KeyType = String

abstract class StringsGenerator(
    private val stringsFileTree: FileTree,
    private val strictLineBreaks: Boolean
) : BaseGenerator<String>() {

    override val inputFiles: Iterable<File> get() = stringsFileTree.files
    override val resourceClassName = ClassName("dev.icerock.moko.resources", "StringResource")
    override val mrObjectName: String = "strings"

    override fun loadLanguageMap(): Map<LanguageType, Map<KeyType, String>> {
        return stringsFileTree.map { file ->
            val language: LanguageType = file.parentFile.name
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
        private val info: SourceInfo,
        private val iosBaseLocalizationRegion: String,
        private val strictLineBreaks: Boolean,
        private val mrSettings: MRGenerator.MRSettings
    ) : ResourceGeneratorFeature<StringsGenerator> {
        private val stringsFileTree =
            info.commonResources.matching { it.include("MR/**/strings*.xml") }

        override fun createCommonGenerator() =
            CommonStringsGenerator(stringsFileTree, strictLineBreaks)

        override fun createIosGenerator() =
            AppleStringsGenerator(stringsFileTree, strictLineBreaks, iosBaseLocalizationRegion)

        override fun createAndroidGenerator() =
            AndroidStringsGenerator(
                stringsFileTree = stringsFileTree,
                strictLineBreaks = strictLineBreaks,
                getAndroidRClassPackage = requireNotNull(info.getAndroidRClassPackage)
            )

        override fun createJsGenerator(): StringsGenerator =
            JsStringsGenerator(stringsFileTree, mrSettings.packageName, strictLineBreaks)

        override fun createJvmGenerator() =
            JvmStringsGenerator(stringsFileTree, strictLineBreaks, mrSettings)
    }
}
