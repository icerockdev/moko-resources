/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.jsGenerator

import dev.icerock.gradle.generator.platform.js.convertToMessageFormat
import kotlin.test.Test
import kotlin.test.assertEquals

class JsMessageFormatTest {
    @Test
    fun cleanStringTest() {
        assertEquals(
            expected = "Clean text with 70% percents.",
            actual = "Clean text with 70% percents.".convertToMessageFormat()
        )
    }

    @Test
    fun cleanStringWithDotInEndTest() {
        assertEquals(
            expected = "Only peoples who like scorpion choose 39%.",
            actual = "Only peoples who like scorpion choose 39%.".convertToMessageFormat()
        )
    }

    @Test
    fun textWithSingleStringArgumentTest() {
        assertEquals(
            expected = "Same guys who like {0} choose pretty woman",
            actual = "Same guys who like %s choose pretty woman".convertToMessageFormat()
        )
    }

    @Test
    fun textWithSingleDigitsArgumentTest() {
        assertEquals(
            expected = "Same guys who like {0} choose pretty woman",
            actual = "Same guys who like %d choose pretty woman".convertToMessageFormat()
        )
    }

    @Test
    fun textWithSingleWithDecimalArgumentTest() {
        assertEquals(
            expected = "Only {0} percents of people like animals",
            actual = "Only %.2d percents of people like animals".convertToMessageFormat()
        )
    }

    @Test
    fun textWithPositionalArgumentsTest() {
        assertEquals(
            expected = "If you find {1} pickles in this garden, you can get {0} as a present",
            actual = "If you find %2$.3f pickles in this garden, you can get %1\$s as a present".convertToMessageFormat()
        )
    }

    @Test
    fun formattedTextWithDecimalArgumentsTest() {
        assertEquals(
            expected = "On your cashback: {0}",
            actual = "On your cashback: %1$5.2f".convertToMessageFormat()
        )
    }

    @Test
    fun sameNonPositionalArgumentsTest() {
        assertEquals(
            expected = "If you grab same artefacts: {0}, {1} and only {2} Gravy with {3} percents you be safety",
            actual = "If you grab same artefacts: %s, %s and only %d Gravy with %.2f percents you be safety".convertToMessageFormat()
        )
    }
}
