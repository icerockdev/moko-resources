/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

import dev.icerock.moko.resources.PluralsResource
import platform.Foundation.NSBundle
import kotlin.test.Test
import kotlin.test.assertEquals

class PluralFormattedStringDescStringTests {
    @Test
    fun testZeroCase() {
        assertEquals(
            expected = "0/10 found",
            actual = createPluralFormatted(0).localized()
        )
    }

    @Test
    fun testOneCase() {
        assertEquals(
            expected = "1/10 item",
            actual = createPluralFormatted(1).localized()
        )
    }

    @Test
    fun testFewCase() {
        assertEquals(
            expected = "3/10 items",
            actual = createPluralFormatted(3).localized()
        )
    }

    @Test
    fun testManyCase() {
        assertEquals(
            expected = "6/10 items",
            actual = createPluralFormatted(6).localized()
        )
    }

    @Test
    fun testOtherCase() {
        assertEquals(
            expected = "130/10 items",
            actual = createPluralFormatted(130).localized()
        )
    }

    @Test
    fun testMissingLocalizationCase() {
        StringDesc.localeType = StringDesc.LocaleType.Custom("es-US")
        assertEquals(
            expected = "6/10 items",
            actual = createPluralFormatted(6).localized()
        )
        StringDesc.localeType = StringDesc.LocaleType.System
    }

    private fun createPluralFormatted(number: Int): PluralFormattedStringDesc {
        val pluralResource = PluralsResource(
            resourceId = "stringFormatted",
            bundle = NSBundle.bundleWithPath(NSBundle.mainBundle.bundlePath + "/tests.bundle")!!
        )
        val counter = "$number/10"
        return PluralFormattedStringDesc(
            pluralsRes = pluralResource,
            number = number,
            args = listOf(counter)
        )
    }
}
