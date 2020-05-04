/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.ClassName
import dev.icerock.gradle.generator.android.AndroidStringsGenerator
import dev.icerock.gradle.generator.common.CommonStringsGenerator
import dev.icerock.gradle.generator.ios.IosStringsGenerator
import org.gradle.api.file.FileTree
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

typealias LanguageType = String
typealias KeyType = String

abstract class StringsGenerator(
    private val stringsFileTree: FileTree
) : BaseGenerator<String>() {
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
        private val iosBaseLocalizationRegion: String
    ) : ResourceGeneratorFeature {
        private val stringsFileTree = info.commonResources.matching { include("MR/**/strings*.xml") }
        override fun createCommonGenerator(): MRGenerator.Generator {
            return CommonStringsGenerator(stringsFileTree)
        }

        override fun createIosGenerator(): MRGenerator.Generator {
            return IosStringsGenerator(
                stringsFileTree,
                iosBaseLocalizationRegion
            )
        }

        override fun createAndroidGenerator(): MRGenerator.Generator {
            return AndroidStringsGenerator(
                stringsFileTree,
                info.androidRClassPackage
            )
        }
    }
}
