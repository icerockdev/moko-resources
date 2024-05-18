/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.stringsGenerator

import dev.icerock.gradle.utils.convertXmlStringToAndroidLocalization
import dev.icerock.gradle.utils.convertXmlStringToLocalization
import kotlin.test.Test
import kotlin.test.assertEquals

class XmlStringsToPlatformRuTest {
    @Test
    fun newLineAndroid() {
        assertEquals(
            expected = """первая строка\nвторая строка""",
            actual = "первая строка\nвторая строка".convertXmlStringToAndroidLocalization()
        )
    }

    @Test
    fun newLineOtherPlatforms() {
        assertEquals(
            expected = """первая строка\nвторая строка""",
            actual = "первая строка\nвторая строка".convertXmlStringToLocalization()
        )
    }

    @Test
    fun textWithApostropheAndroid() {
        assertEquals(
            expected = """Я\&apos;ж купил новый 27 дюйм\&apos;ов монитор""",
            actual = "Я'ж купил новый 27 дюйм'ов монитор".convertXmlStringToAndroidLocalization()
        )
    }

    @Test
    fun textWithApostropheOtherPlatforms() {
        assertEquals(
            expected = """Я'ж купил новый 27 дюйм'ов монитор""",
            actual = "Я'ж купил новый 27 дюйм'ов монитор".convertXmlStringToLocalization()
        )
    }

    @Test
    fun textWithXmlTagsAndroid() {
        assertEquals(
            expected = """Текст with &lt;b&gt;жирный&lt;/b&gt;, &lt;i&gt;курсив&lt;/i&gt;, &lt;u&gt;подчеркнутый&lt;/u&gt;""",
            actual = "Текст with &lt;b&gt;жирный&lt;/b&gt;, &lt;i&gt;курсив&lt;/i&gt;, &lt;u&gt;подчеркнутый&lt;/u&gt;".convertXmlStringToAndroidLocalization(),
        )
    }

    @Test
    fun textWithXmlTagsOtherPlatforms() {
        assertEquals(
            expected = """Текст с <b>жирный</b>, <i>курсив</i>, <u>подчеркнутый</u>""",
            actual = "Текст с &lt;b&gt;жирный&lt;/b&gt;, &lt;i&gt;курсив&lt;/i&gt;, &lt;u&gt;подчеркнутый&lt;/u&gt;".convertXmlStringToLocalization(),
        )
    }
}
