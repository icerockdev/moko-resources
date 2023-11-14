/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.generator.android.AndroidColorsGenerator
import dev.icerock.gradle.generator.apple.AppleColorsGenerator
import dev.icerock.gradle.generator.common.CommonColorsGenerator
import dev.icerock.gradle.generator.js.JsColorsGenerator
import dev.icerock.gradle.generator.jvm.JvmColorsGenerator
import org.gradle.api.file.FileTree
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

abstract class ColorsGenerator(
    private val colorsFileTree: FileTree,
) : MRGenerator.Generator {

    override val inputFiles: Iterable<File> get() = colorsFileTree.files
    override val resourceClassName: ClassName =
        ClassName("dev.icerock.moko.resources", "ColorResource")
    override val mrObjectName: String = "colors"

    private val colorClassName =
        ClassName("dev.icerock.moko.resources", "ColorResource")

    open fun beforeGenerate(objectBuilder: TypeSpec.Builder, keys: List<String>) {}

    @Suppress("SpreadOperator")
    override fun generate(
        assetsGenerationDir: File,
        resourcesGenerationDir: File,
        objectBuilder: TypeSpec.Builder,
    ): TypeSpec {
        objectBuilder.addModifiers(*getClassModifiers())
        extendObjectBodyAtStart(objectBuilder)

        val colors = parseColors()

        beforeGenerate(objectBuilder, colors.map { it.name })

        colors.forEach { colorNode ->
            val property = PropertySpec.builder(colorNode.name, colorClassName)
            property.addModifiers(*getPropertyModifiers())
            getPropertyInitializer(colorNode)?.let { property.initializer(it) }
            objectBuilder.addProperty(property.build())
        }

        generateResources(resourcesGenerationDir, colors)

        extendObjectBodyAtEnd(objectBuilder)

        return objectBuilder.build()
    }

    protected open fun getClassModifiers(): Array<KModifier> = emptyArray()
    protected open fun getPropertyModifiers(): Array<KModifier> = emptyArray()

    abstract fun getPropertyInitializer(color: ColorNode): CodeBlock?

    protected open fun generateResources(
        resourcesGenerationDir: File,
        colors: List<ColorNode>,
    ) = Unit

    private fun parseColors(): List<ColorNode> {
        val colorNodes = mutableListOf<ColorNode>()
        val colorValues = mutableMapOf<String, String>()

        fun getColor(color: String?): String? {
            return if (color?.startsWith(XmlColorReferencePrefix) == true) {
                val colorName = color.replace(XmlColorReferencePrefix, "")
                val colorValue = colorValues[colorName]
                getColor(colorValue)
            } else {
                val rawColor = color?.removePrefix("#")?.removePrefix("0x")
                if (rawColor?.length == RgbFormatLength) "${rawColor}FF" else rawColor
            }
        }

        colorsFileTree.map { file ->
            val dbFactory = DocumentBuilderFactory.newInstance()
            val dBuilder = dbFactory.newDocumentBuilder()
            val doc = dBuilder.parse(file)

            val stringNodes = doc.getElementsByTagName(XmlColorTag)

            for (i in 0 until stringNodes.length) {
                val stringNode = stringNodes.item(i)

                val colorName = stringNode.attributes.getNamedItem(XmlNodeAttrColorName).textContent
                var lightColor: String? = null
                var darkColor: String? = null
                var singleColor: String? = null
                val nodeList: NodeList = stringNode.childNodes
                for (nodeIdx in 0 until nodeList.length) {
                    val xmlNode: Node = nodeList.item(nodeIdx)
                    when (xmlNode.nodeName) {
                        "light" -> {
                            lightColor = xmlNode.textContent
                        }

                        "dark" -> {
                            darkColor = xmlNode.textContent
                        }

                        else -> {
                            singleColor = xmlNode.textContent
                            singleColor?.let {
                                colorValues[colorName] = it
                            }
                        }
                    }
                }
                colorNodes.add(
                    ColorNode(
                        colorName,
                        getColor(lightColor),
                        getColor(darkColor),
                        getColor(singleColor)
                    )
                )
            }
        }

        return colorNodes
    }

    class Feature(
        private val settings: MRGenerator.Settings,
    ) : ResourceGeneratorFeature<ColorsGenerator> {
        private val fileTree: FileTree = settings.ownResourcesFileTree
            .matching { it.include("**/colors*.xml") }

        override fun createCommonGenerator() = CommonColorsGenerator(
            ownColorsFileTree = settings.ownResourcesFileTree,
            upperColorsFileTree = settings.upperResourcesFileTree
        )

        override fun createIosGenerator() = AppleColorsGenerator(
            ownColorsFileTree = settings.ownResourcesFileTree,
            lowerColorsFileTree = settings.lowerResourcesFileTree
        )

        override fun createAndroidGenerator() = AndroidColorsGenerator(
            ownColorsFileTree = settings.ownResourcesFileTree,
            lowerColorsFileTree = settings.lowerResourcesFileTree
        )

        override fun createJsGenerator(): ColorsGenerator = JsColorsGenerator(
            ownColorsFileTree = settings.ownResourcesFileTree,
            lowerColorsFileTree = settings.lowerResourcesFileTree
        )

        override fun createJvmGenerator() = JvmColorsGenerator(
            ownColorsFileTree = settings.ownResourcesFileTree,
            lowerColorsFileTree = settings.lowerResourcesFileTree, settings
        )
    }

    protected fun replaceColorAlpha(color: String?): String? {
        if (color == null) return null

        val alpha: String = if (isRgbFormat(color)) {
            DefaultAlpha
        } else {
            color.substring(color.length - 2, color.length)
        }

        return if (isRgbFormat(color)) {
            "$alpha$color"
        } else {
            "$alpha${color.removeRange(color.length - 2, color.length)}"
        }
    }

    private fun isRgbFormat(color: String): Boolean = color.length == RgbFormatLength

    companion object {
        internal const val XmlColorTag = "color"
        internal const val XmlNodeAttrColorName = "name"
        internal const val XmlColorReferencePrefix = "@color/"
        internal const val RgbFormatLength = 6
        internal const val DefaultAlpha = "FF"
    }
}

data class ColorNode(
    val name: String,
    val lightColor: String?, // as rgba
    val darkColor: String?, // as rgba
    val singleColor: String?, // as rgba
) {
    fun isThemed(): Boolean = lightColor != null && darkColor != null
}
