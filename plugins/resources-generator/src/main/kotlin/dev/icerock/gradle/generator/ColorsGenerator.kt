/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import com.android.utils.forEach
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.generator.android.AndroidColorsGenerator
import dev.icerock.gradle.generator.common.CommonColorsGenerator
import dev.icerock.gradle.generator.ios.IosColorsGenerator
import org.gradle.api.file.FileTree
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

abstract class ColorsGenerator(
    private val colorsFileTree: FileTree
) : MRGenerator.Generator {

    override val resourceClassName: ClassName =
        ClassName("dev.icerock.moko.resources", "ColorResource")
    override val mrObjectName: String = "colors"

    protected val singleColorClassName =
        ClassName("dev.icerock.moko.resources", "ColorResource.Single")
    protected val themedColorClassName =
        ClassName("dev.icerock.moko.resources", "ColorResource.Themed")

    @Suppress("SpreadOperator")
    override fun generate(resourcesGenerationDir: File, objectBuilder: TypeSpec.Builder): TypeSpec {
        objectBuilder.addModifiers(*getClassModifiers())
        extendObjectBody(objectBuilder)

        val colors = parseColors()
        colors.forEach { colorNode ->
            val className = if (colorNode.isThemed()) {
                themedColorClassName
            } else {
                singleColorClassName
            }
            val property = PropertySpec.builder(colorNode.name, className)
            property.addModifiers(*getPropertyModifiers())
            getPropertyInitializer(colorNode)?.let { property.initializer(it) }
            objectBuilder.addProperty(property.build())
        }

        generateResources(resourcesGenerationDir, colors)

        return objectBuilder.build()
    }

    override fun extendObjectBody(classBuilder: TypeSpec.Builder) = Unit

    protected open fun getClassModifiers(): Array<KModifier> = emptyArray()
    protected open fun getPropertyModifiers(): Array<KModifier> = emptyArray()
    protected open fun getPropertyInitializer(color: ColorNode): CodeBlock? {
        val className = if (color.isThemed()) {
            "Themed(light = Color(0x${color.lightColor}), dark = Color(0x${color.darkColor}))"
        } else {
            "Single(color = Color(0x${color.singleColor}))"
        }
        return CodeBlock.of(className)
    }

    protected open fun generateResources(
        resourcesGenerationDir: File,
        colors: List<ColorNode>
    ) = Unit

    private fun parseColors(): List<ColorNode> {
        val colorNodes = mutableListOf<ColorNode>()
        val colorValues = mutableMapOf<String, String>()

        fun getColor(color: String?): String? {
            return if (color?.startsWith(XmlColorReferencePrefix) == true) {
                val colorName = color.replace(XmlColorReferencePrefix, "")
                colorValues[colorName]
            } else {
                color
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
                stringNode.childNodes.forEach { xmlNode ->
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
                                if (!it.startsWith(XmlColorReferencePrefix)) {
                                    colorValues[colorName] = it
                                }
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
        private val info: SourceInfo
    ) : ResourceGeneratorFeature<ColorsGenerator> {

        private val colorsFileTree =
            info.commonResources.matching { it.include("MR/**/colors*.xml") }

        override fun createCommonGenerator(): ColorsGenerator {
            return CommonColorsGenerator(colorsFileTree)
        }

        override fun createIosGenerator(): ColorsGenerator {
            return IosColorsGenerator(
                colorsFileTree
            )
        }

        override fun createAndroidGenerator(): ColorsGenerator {
            return AndroidColorsGenerator(
                colorsFileTree
            )
        }
    }

    protected fun replaceColorAlpha(color: String?): String? {
        if (color == null) return color

        val alpha = color.substring(color.length - 2, color.length)
        return "$alpha${color.removeRange(color.length - 2, color.length)}"
    }

    companion object {
        internal const val XmlColorTag = "color"
        internal const val XmlNodeAttrColorName = "name"
        internal const val XmlColorReferencePrefix = "@color/"
    }
}

data class ColorNode(
    val name: String,
    val lightColor: String?, // as rgba
    val darkColor: String?, // as rgba
    val singleColor: String? // as rgba
) {
    fun isThemed(): Boolean = lightColor != null && darkColor != null
}
