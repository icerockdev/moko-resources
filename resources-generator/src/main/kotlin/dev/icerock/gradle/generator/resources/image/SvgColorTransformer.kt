/*
 * Copyright 2026 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.resources.image

import java.util.Locale

@Suppress("MagicNumber")
internal object SvgColorTransformer {
    private const val HEX_8_LENGTH = 8
    private const val HEX_4_LENGTH = 4

    @Suppress("LongMethod")
    fun transform(content: String): String {
        val attrNamesPattern = ColorAttribute.allNames.joinToString("|")
        val attrRegex = Regex("""($attrNamesPattern)\s*=\s*(['"])#([0-9a-fA-F]+)\2""")

        var result = attrRegex.replace(content) { matchResult ->
            val attrName = matchResult.groupValues[1]
            val quote = matchResult.groupValues[2]
            val hex = matchResult.groupValues[3]
            val colorAttr = ColorAttribute.findByName(attrName)

            when (hex.length) {
                HEX_8_LENGTH -> {
                    val rgb = hex.substring(0, 6)
                    val alpha = hex.substring(6, 8)
                    val alphaStr = hexToAlpha(alpha)
                    val opacityAttr = colorAttr?.opacityAttrName

                    if (opacityAttr != null) {
                        """$attrName=$quote#$rgb$quote $opacityAttr=$quote$alphaStr$quote"""
                    } else {
                        """$attrName=$quote#$rgb$quote"""
                    }
                }
                HEX_4_LENGTH -> {
                    val rgbShort = hex.substring(0, 3)
                    val alpha = hex.substring(3, 4)
                    val rgb = rgbShort.map { "$it$it" }.joinToString("")
                    val alphaStr = hexToAlpha("$alpha$alpha")
                    val opacityAttr = colorAttr?.opacityAttrName

                    if (opacityAttr != null) {
                        """$attrName=$quote#$rgb$quote $opacityAttr=$quote$alphaStr$quote"""
                    } else {
                        """$attrName=$quote#$rgb$quote"""
                    }
                }
                else -> matchResult.value
            }
        }

        val styleAttrRegex = Regex("""style\s*=\s*(['"])([^"']*)\1""")
        result = styleAttrRegex.replace(result) { matchResult ->
            val quote = matchResult.groupValues[1]
            val styleContent = matchResult.groupValues[2]

            val colorRegex = Regex("""($attrNamesPattern)\s*:\s*#([0-9a-fA-F]+)""")
            val transformedStyle = colorRegex.replace(styleContent) { colorMatch ->
                val attrName = colorMatch.groupValues[1]
                val hex = colorMatch.groupValues[2]
                val colorAttr = ColorAttribute.findByName(attrName)

                when (hex.length) {
                    HEX_8_LENGTH -> {
                        val rgb = hex.substring(0, 6)
                        val alpha = hex.substring(6, 8)
                        val alphaStr = hexToAlpha(alpha)
                        val opacityAttr = colorAttr?.opacityAttrName

                        if (opacityAttr != null) {
                            """$attrName:#$rgb;$opacityAttr:$alphaStr"""
                        } else {
                            """$attrName:#$rgb"""
                        }
                    }
                    HEX_4_LENGTH -> {
                        val rgbShort = hex.substring(0, 3)
                        val alpha = hex.substring(3, 4)
                        val rgb = rgbShort.map { "$it$it" }.joinToString("")
                        val alphaStr = hexToAlpha("$alpha$alpha")
                        val opacityAttr = colorAttr?.opacityAttrName

                        if (opacityAttr != null) {
                            """$attrName:#$rgb;$opacityAttr:$alphaStr"""
                        } else {
                            """$attrName:#$rgb"""
                        }
                    }
                    else -> colorMatch.value
                }
            }
            """style=$quote$transformedStyle$quote"""
        }

        return result
    }

    private fun hexToAlpha(hex: String): String {
        val alphaInt = hex.toInt(16)
        val alphaFloat = alphaInt / 255.0
        return "%.2f".format(Locale.US, alphaFloat)
    }
}

private enum class ColorAttribute(val attrName: String, val opacityAttrName: String?) {
    FILL(attrName = "fill", opacityAttrName = "fill-opacity"),
    STROKE(attrName = "stroke", opacityAttrName = "stroke-opacity"),
    STOP_COLOR(attrName = "stop-color", opacityAttrName = "stop-opacity"),
    FLOOD_COLOR(attrName = "flood-color", opacityAttrName = "flood-opacity"),
    COLOR(attrName = "color", opacityAttrName = null);

    companion object {
        val allNames: List<String> = entries.map { it.attrName }
        fun findByName(name: String): ColorAttribute? = entries.find { it.attrName == name }
    }
}
