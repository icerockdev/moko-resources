/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.stringsGenerator

import dev.icerock.gradle.utils.convertXmlStringToAndroidLocalization
import dev.icerock.gradle.utils.convertXmlStringToApplePluralLocalization
import dev.icerock.gradle.utils.convertXmlStringToLocalization
import kotlin.test.Test
import kotlin.test.assertEquals

class XmlStringsToPlatformTest {
    @Test
    fun newLineAndroid() {
        assertEquals(
            expected = """first line\nsecond line""",
            actual = "first line\nsecond line".convertXmlStringToAndroidLocalization()
        )
    }

    @Test
    fun newLineOtherPlatforms() {
        assertEquals(
            expected = """first line\nsecond line""",
            actual = "first line\nsecond line".convertXmlStringToLocalization()
        )
    }

    @Test
    fun separateSymbolsAndroid() {
        assertEquals(
            expected = """\&quot; \&apos; % @ * &amp; {}""",
            actual = "\" ' % @ * &amp; {}".convertXmlStringToAndroidLocalization()
        )
    }

    @Test
    fun separateSymbolsOtherPlatforms() {
        assertEquals(
            expected = """\" ' % @ * & {}""",
            actual = "\" ' % @ * &amp; {}".convertXmlStringToLocalization()
        )
    }

    @Test
    fun textWithApostropheAndroid() {
        assertEquals(
            expected = """I\&apos;m bought new monitor with 27 inch\&apos;s""",
            actual = "I'm bought new monitor with 27 inch's".convertXmlStringToAndroidLocalization()
        )
    }

    @Test
    fun textWithApostropheOtherPlatforms() {
        assertEquals(
            expected = """I'm bought new monitor with 27 inch's""",
            actual = "I'm bought new monitor with 27 inch's".convertXmlStringToLocalization()
        )
    }

    @Test
    fun textWithXmlTagsAndroid() {
        assertEquals(
            expected = """Text with &lt;b&gt;bold&lt;/b&gt;, &lt;i&gt;italic&lt;/i&gt;, &lt;u&gt;underline&lt;/u&gt;""",
            actual = "Text with &lt;b&gt;bold&lt;/b&gt;, &lt;i&gt;italic&lt;/i&gt;, &lt;u&gt;underline&lt;/u&gt;".convertXmlStringToAndroidLocalization(),
        )
    }

    @Test
    fun textWithXmlTagsOtherPlatforms() {
        assertEquals(
            expected = """Text with <b>bold</b>, <i>italic</i>, <u>underline</u>""",
            actual = "Text with &lt;b&gt;bold&lt;/b&gt;, &lt;i&gt;italic&lt;/i&gt;, &lt;u&gt;underline&lt;/u&gt;".convertXmlStringToLocalization(),
        )
    }

    @Test
    fun textWithQuotesAndroid() {
        assertEquals(
            expected = """%d count \&quot;%s\&quot;""",
            actual = "%d count \"%s\"".convertXmlStringToAndroidLocalization()
        )
    }

    @Test
    fun textWithQuotesOtherPlatforms() {
        assertEquals(
            expected = """%d count \&quot;%s\&quot;""",
            actual = "%d count \"%s\"".convertXmlStringToAndroidLocalization()
        )
    }

    @Test
    fun unicodeEmojiAppleTest() {
        assertEquals(
            expected = """😈""",
            actual = "\uD83D\uDE08".convertXmlStringToLocalization()
        )
    }

    @Test
    fun unicodeEmojiApplePluralTest() {
        assertEquals(
            expected = """😈""",
            actual = "\uD83D\uDE08".convertXmlStringToApplePluralLocalization()
        )
    }

    @Test
    fun unicodeEmojiAndroidTest() {
        assertEquals(
            expected = """😈""",
            actual = "\uD83D\uDE08".convertXmlStringToAndroidLocalization()
        )
    }

    @Test
    fun unicodeEmojiOtherPlatformTest() {
        assertEquals(
            expected = """😈""",
            actual = "\uD83D\uDE08".convertXmlStringToLocalization()
        )
    }

    @Test
    fun stringLikeAndroidLinkOnStringAndroidTest() {
        assertEquals(
            expected = """\@same text""",
            actual = """@same text""".convertXmlStringToAndroidLocalization()
        )
    }
}
