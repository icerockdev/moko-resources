/*
 * Copyright 2026 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.localization

import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals

class LanguageTypeTest {

    @Test
    fun `base language`() {
        assertOutputs(
            fileName = "base",
            android = "values",
            apple = "Base.lproj",
            jvm = "",
            js = ""
        )
    }

    @Test
    fun `language only`() {
        assertOutputs(
            fileName = "en",
            android = "values-en",
            apple = "en.lproj",
            jvm = "_en",
            js = "_en"
        )
    }

    @Test
    fun `language and region in android format`() {
        assertOutputs(
            fileName = "en-rUS",
            android = "values-en-rUS",
            apple = "en-US.lproj",
            jvm = "_en_US",
            js = "_en-US"
        )
    }

    @Test
    fun `language and region in bcp47 format`() {
        assertOutputs(
            fileName = "en-US",
            android = "values-en-rUS",
            apple = "en-US.lproj",
            jvm = "_en_US",
            js = "_en-US"
        )
    }

    @Test
    fun `language with legacy iso 639 code`() {
        assertOutputs(
            fileName = "he",
            android = "values-iw",
            apple = "he.lproj",
            // JDK before 17 returns the legacy `iw` code for `he`
            jvm = "_${Locale.forLanguageTag("he").language}",
            js = "_he"
        )
    }

    @Test
    fun `language and script`() {
        assertOutputs(
            fileName = "sr-Latn",
            android = "values-b+sr+Latn",
            apple = "sr-Latn.lproj",
            jvm = "_sr_Latn",
            js = "_sr-Latn"
        )
    }

    @Test
    fun `language script and region`() {
        assertOutputs(
            fileName = "sr-Latn-RS",
            android = "values-b+sr+Latn+RS",
            apple = "sr-Latn-RS.lproj",
            jvm = "_sr_Latn_RS",
            js = "_sr-Latn-RS"
        )
    }

    @Test
    fun `language script and region in android format`() {
        assertOutputs(
            fileName = "sr-Latn-rRS",
            android = "values-b+sr+Latn+RS",
            apple = "sr-Latn-RS.lproj",
            jvm = "_sr_Latn_RS",
            js = "_sr-Latn-RS"
        )
    }

    @Test
    fun `chinese simplified script`() {
        assertOutputs(
            fileName = "zh-Hans",
            android = "values-b+zh+Hans",
            apple = "zh-Hans.lproj",
            jvm = "_zh_Hans",
            js = "_zh-Hans"
        )
    }

    private fun assertOutputs(
        fileName: String,
        android: String,
        apple: String,
        jvm: String,
        js: String
    ) {
        val languageType = LanguageType.fromFileName(fileName)
        assertEquals(expected = android, actual = languageType.androidResourcesDir, message = "android")
        assertEquals(expected = apple, actual = languageType.appleResourcesDir, message = "apple")
        assertEquals(expected = jvm, actual = languageType.jvmResourcesSuffix, message = "jvm")
        assertEquals(expected = js, actual = languageType.jsResourcesSuffix, message = "js")
    }
}
