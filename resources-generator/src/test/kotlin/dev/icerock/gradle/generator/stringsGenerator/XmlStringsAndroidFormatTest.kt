/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.stringsGenerator

import dev.icerock.gradle.utils.convertXmlStringToAndroidLocalization
import dev.icerock.gradle.utils.removeAndroidMirroringFormat
import kotlin.test.Test
import kotlin.test.assertEquals

class XmlStringsAndroidFormatTest {
    @Test
    fun quotesAndroidFormat() {
        assertEquals(
            expected = """Same text with symbol's @ ? somet'ing "word"""",
            actual = TEXT_WITH_QUOTES.removeAndroidMirroringFormat()
        )
    }

    @Test
    fun quotesAndroidInOutFormat() {
        assertEquals(
            expected = """Same text with symbol\&apos;s @ ? somet\&apos;ing \&quot;word\&quot;""",
            actual = TEXT_WITH_QUOTES
                .removeAndroidMirroringFormat()
                .convertXmlStringToAndroidLocalization()
        )
    }

    private companion object {
        private const val TEXT_WITH_QUOTES = """Same text with symbol's @ ? somet\'ing \"word""""

    }
}
