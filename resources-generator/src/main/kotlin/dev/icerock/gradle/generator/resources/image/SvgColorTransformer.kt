/*
 * Copyright 2026 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.resources.image

import java.util.Locale

@Suppress("MagicNumber")
internal object SvgColorTransformer {
    private const val LONG_HEX_WITH_ALPHA_LENGTH = 8
    private const val SHORT_HEX_WITH_ALPHA_LENGTH = 4
    private const val MAX_ALPHA_CHANNEL_VALUE = 255.0

    fun transform(content: String): String {
        val attrNamesPattern = ColorAttribute.allNames.joinToString("|")
        val attrRegex = Regex("""($attrNamesPattern)\s*=\s*(['"])#([0-9a-fA-F]+)\2""")

        var result = attrRegex.replace(content) { matchResult ->
            val attrName = matchResult.groupValues[1]
            val quote = matchResult.groupValues[2]
            val hex = matchResult.groupValues[3]

            val color = parseColorWithAlpha(hex) ?: return@replace matchResult.value
            formatXmlAttribute(attrName = attrName, quote = quote, color = color)
        }

        val styleAttrRegex = Regex("""style\s*=\s*(['"])([^"']*)\1""")
        result = styleAttrRegex.replace(result) { matchResult ->
            val quote = matchResult.groupValues[1]
            val styleContent = matchResult.groupValues[2]

            val colorRegex = Regex("""($attrNamesPattern)\s*:\s*#([0-9a-fA-F]+)""")
            val transformedStyle = colorRegex.replace(styleContent) { colorMatch ->
                val attrName = colorMatch.groupValues[1]
                val hex = colorMatch.groupValues[2]

                val color = parseColorWithAlpha(hex) ?: return@replace colorMatch.value
                formatStyleProperty(attrName = attrName, color = color)
            }
            """style=$quote$transformedStyle$quote"""
        }

        return result
    }

    private fun formatXmlAttribute(
        attrName: String,
        quote: String,
        color: SvgColorWithAlpha,
    ): String {
        val opacityAttrName = ColorAttribute.findByName(attrName).opacityAttrName

        return """$attrName=$quote#${color.rgb}$quote $opacityAttrName=$quote${color.alpha}$quote"""
    }

    private fun formatStyleProperty(
        attrName: String,
        color: SvgColorWithAlpha,
    ): String {
        val opacityAttrName = ColorAttribute.findByName(attrName).opacityAttrName

        return "$attrName:#${color.rgb};$opacityAttrName:${color.alpha}"
    }

    private fun parseColorWithAlpha(hex: String): SvgColorWithAlpha? {
        return when (hex.length) {
            LONG_HEX_WITH_ALPHA_LENGTH -> {
                SvgColorWithAlpha(
                    rgb = hex.substring(0, 6),
                    alpha = formatAlpha(hex.substring(6, 8))
                )
            }

            SHORT_HEX_WITH_ALPHA_LENGTH -> {
                val rgb = hex.take(3).map { "$it$it" }.joinToString(separator = "")
                val alpha = hex.last().let { "$it$it" }

                SvgColorWithAlpha(
                    rgb = rgb,
                    alpha = formatAlpha(alpha)
                )
            }

            else -> null
        }
    }

    private fun formatAlpha(hex: String): String {
        val alphaFloat = hex.toInt(16) / MAX_ALPHA_CHANNEL_VALUE
        return "%.2f".format(Locale.US, alphaFloat)
    }

    private data class SvgColorWithAlpha(
        val rgb: String,
        val alpha: String,
    )
}

private enum class ColorAttribute(val attrName: String, val opacityAttrName: String) {
    FILL(attrName = "fill", opacityAttrName = "fill-opacity"),
    STROKE(attrName = "stroke", opacityAttrName = "stroke-opacity"),
    STOP_COLOR(attrName = "stop-color", opacityAttrName = "stop-opacity"),
    FLOOD_COLOR(attrName = "flood-color", opacityAttrName = "flood-opacity");

    companion object {
        val allNames: List<String> = entries.map { it.attrName }
        fun findByName(name: String): ColorAttribute = entries.first { it.attrName == name }
    }
}
