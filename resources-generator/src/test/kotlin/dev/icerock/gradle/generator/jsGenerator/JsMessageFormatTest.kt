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

    // That case doesn't supported in js target,
    // but so close with regex of message format
    @Test
    fun textWithSingleWithDecimalArgumentTest() {
        assertEquals(
            expected = "Only %.2d percents of people like animals",
            actual = "Only %.2d percents of people like animals".convertToMessageFormat()
        )
    }

    @Test
    fun textWithPositionalArgumentsTest() {
        assertEquals(
            expected = "If you find {0} pickles in this garden, you can get {1} as a present",
            actual = "If you find %$1d pickles in this garden, you can get %$2s as a present".convertToMessageFormat()
        )
    }
}
