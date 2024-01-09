/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.color

import com.squareup.kotlinpoet.PropertySpec
import dev.icerock.gradle.generator.CodeConst
import dev.icerock.gradle.generator.ResourceGenerator
import dev.icerock.gradle.metadata.resource.ColorMetadata
import org.gradle.api.GradleException
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

internal class ColorResourceGenerator : ResourceGenerator<ColorMetadata> {

    override fun generateMetadata(files: Set<File>): List<ColorMetadata> {
        val result: MutableList<ColorMetadata> = mutableListOf()
        val colorValues: MutableMap<String, String> = mutableMapOf()

        files.forEach { file ->
            val dbFactory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
            val dBuilder: DocumentBuilder = dbFactory.newDocumentBuilder()
            val doc: Document = dBuilder.parse(file)

            val stringNodes: NodeList = doc.getElementsByTagName(XmlColorTag)

            for (i: Int in 0 until stringNodes.length) {
                val stringNode: Node = stringNodes.item(i)

                val metadata: ColorMetadata = readColorValue(
                    stringNode = stringNode,
                    colorValues = colorValues
                )
                result.add(metadata)
            }
        }

        return result
    }

    private fun readColorValue(
        stringNode: Node,
        colorValues: MutableMap<String, String>
    ): ColorMetadata {
        val colorName: String = stringNode.attributes.getNamedItem(XmlNodeAttrColorName).textContent

        var lightColor: String? = null
        var darkColor: String? = null
        var singleColor: String? = null
        val nodeList: NodeList = stringNode.childNodes
        for (nodeIdx: Int in 0 until nodeList.length) {
            val xmlNode: Node = nodeList.item(nodeIdx)
            when (xmlNode.nodeName) {
                "light" -> {
                    lightColor = colorValues.parseColor(xmlNode.textContent)
                }

                "dark" -> {
                    darkColor = colorValues.parseColor(xmlNode.textContent)
                }

                else -> {
                    if (xmlNode.textContent.isBlank()) continue

                    singleColor = colorValues.parseColor(xmlNode.textContent)
                        .also { colorValues[colorName] = it }
                }
            }
        }

        val item: ColorMetadata.ColorItem = if (singleColor != null) {
            ColorMetadata.ColorItem.Single(
                color = singleColor.toColor()
            )
        } else {
            ColorMetadata.ColorItem.Themed(
                light = requireNotNull(lightColor) { "light color should be set for themed color" }
                    .toColor(),
                dark = requireNotNull(darkColor) { "dark color should be set for themed color" }
                    .toColor()
            )
        }

        return ColorMetadata(
            key = colorName,
            value = item
        )
    }

    override fun generateProperty(metadata: ColorMetadata): PropertySpec.Builder {
        return PropertySpec.builder(metadata.key, CodeConst.colorResourceName)
    }

    private fun Map<String, String>.parseColor(color: String): String {
        return if (color.startsWith(XmlColorReferencePrefix)) {
            val colorName: String = color.replace(XmlColorReferencePrefix, "")
            val colorValue: String = this[colorName]
                ?: error("color reference $colorName not found")
            parseColor(colorValue)
        } else {
            val rawColor: String = color.removePrefix("#").removePrefix("0x")
            return if (rawColor.length == RgbFormatLength) "${rawColor}${DefaultAlpha}"
            else rawColor
        }
    }

    private fun String.toColor(): ColorMetadata.Color {
        val rgbaColor: Long = try {
            this.toLong(16)
        } catch (exc: Exception) {
            throw GradleException("can't parse $this to ColorMetadata.Color", exc)
        }
        return ColorMetadata.Color(
            red = (rgbaColor shr 24 and 0xff).toInt(),
            green = (rgbaColor shr 16 and 0xff).toInt(),
            blue = (rgbaColor shr 8 and 0xff).toInt(),
            alpha = (rgbaColor and 0xff).toInt()
        )
    }

    private companion object {
        const val XmlColorTag = "color"
        const val XmlNodeAttrColorName = "name"
        const val XmlColorReferencePrefix = "@color/"
        const val RgbFormatLength = 6
        const val DefaultAlpha = "FF"
    }
}
