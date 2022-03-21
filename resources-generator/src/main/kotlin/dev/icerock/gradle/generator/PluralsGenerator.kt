/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.ClassName
import dev.icerock.gradle.generator.android.AndroidPluralsGenerator
import dev.icerock.gradle.generator.common.CommonPluralsGenerator
import dev.icerock.gradle.generator.apple.ApplePluralsGenerator
import dev.icerock.gradle.generator.jvm.JvmPluralsGenerator
import dev.icerock.gradle.utils.removeLineWraps
import org.gradle.api.file.FileTree
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

typealias PluralMap = Map<String, String>

abstract class PluralsGenerator(
    private val pluralsFileTree: FileTree,
    private val strictLineBreaks: Boolean
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

                pluralMap[quantity] = if (strictLineBreaks) value else value.removeLineWraps()
            }

            mutableMap[name] = pluralMap
        }

        return mutableMap
    }

    class Feature(
        private val info: SourceInfo,
        private val iosBaseLocalizationRegion: String,
        private val mrClassPackage: String,
        private val strictLineBreaks: Boolean
    ) : ResourceGeneratorFeature<PluralsGenerator> {
        private val stringsFileTree = info.commonResources.matching { it.include("MR/**/plurals*.xml") }
        override fun createCommonGenerator(): PluralsGenerator =
            CommonPluralsGenerator(stringsFileTree, strictLineBreaks)

        override fun createIosGenerator(): PluralsGenerator =
            ApplePluralsGenerator(stringsFileTree, strictLineBreaks, iosBaseLocalizationRegion)

        override fun createAndroidGenerator(): PluralsGenerator =
            AndroidPluralsGenerator(stringsFileTree, strictLineBreaks, info.androidRClassPackage)

        override fun createJvmGenerator() =
            JvmPluralsGenerator(stringsFileTree, strictLineBreaks, mrClassPackage)
    }
}
