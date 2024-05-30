/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.pluralsGenerator

import dev.icerock.gradle.utils.convertXmlStringToAndroidLocalization
import dev.icerock.gradle.utils.convertXmlStringToApplePluralLocalization
import dev.icerock.gradle.utils.convertXmlStringToLocalization
import kotlin.test.Test
import kotlin.test.assertEquals

class XmlPluralsToPlatformTest {
    @Test
    fun simplePluralsAppleTest(){
        assertEquals(
            expected = """%d count of &quot;%s&quot;""",
            actual ="%d count of \"%s\"".convertXmlStringToApplePluralLocalization(),
        )
    }

    @Test
    fun simplePluralsAndroidTest(){
        assertEquals(
            expected = """%d count of \&quot;%s\&quot;""",
            actual ="""%d count of "%s"""".convertXmlStringToAndroidLocalization(),
        )
    }

    @Test
    fun simplePluralsOtherPlatformsTest() {
        assertEquals(
            expected = """%d count of \"%s\"""",
            actual ="%d count of \"%s\"".convertXmlStringToLocalization(),
        )
    }

    @Test
    fun pluralWithNewLineAppleTest() {
        assertEquals(
            expected = """%d count
                |of tests""".trimMargin(),
            actual ="%d count\nof tests".convertXmlStringToApplePluralLocalization(),
        )
    }

    @Test
    fun pluralWithNewLineAndroidTest() {
        assertEquals(
            expected = """%d count\nof tests""",
            actual ="%d count\nof tests".convertXmlStringToAndroidLocalization(),
        )
    }

    @Test
    fun pluralWithNewLineOtherPlatformTest() {
        assertEquals(
            expected = """%d count\nof tests""",
            actual ="%d count\nof tests".convertXmlStringToLocalization(),
        )
    }

    @Test
    fun separateSymbolsApplePluralsTest() {
        assertEquals(
            expected = """&quot; &apos; % @ * &amp; {}""",
            actual = "\" ' % @ * &amp; {}".convertXmlStringToApplePluralLocalization()
        )
    }
}
