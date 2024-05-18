/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.pluralsGenerator

import dev.icerock.gradle.utils.convertXmlStringToAndroidLocalization
import dev.icerock.gradle.utils.convertXmlStringToApplePluralLocalization
import dev.icerock.gradle.utils.convertXmlStringToLocalization
import kotlin.test.Test
import kotlin.test.assertEquals

class XmlPluralsToPlatformRuTest {
    @Test
    fun simplePluralsAppleTest(){
        assertEquals(
            expected = """%d число &quot;%s&quot;""",
            actual ="%d число \"%s\"".convertXmlStringToApplePluralLocalization(),
        )
    }

    @Test
    fun simplePluralsAndroidTest(){
        assertEquals(
            expected = """%d число \&quot;%s\&quot;""",
            actual ="%d число \"%s\"".convertXmlStringToAndroidLocalization(),
        )
    }

    @Test
    fun simplePluralsOtherPlatformsTest() {
        assertEquals(
            expected = """%d число \"%s\"""",
            actual ="%d число \"%s\"".convertXmlStringToLocalization(),
        )
    }
}
