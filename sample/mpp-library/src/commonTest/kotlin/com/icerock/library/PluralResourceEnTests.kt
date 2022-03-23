/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerock.library

import com.icerockdev.library.MR
import com.icerockdev.library.Testing
import dev.icerock.moko.resources.desc.desc
import kotlin.test.Ignore
import kotlin.test.Test

class PluralResourceEnTests : BaseStringResourceTests("en") {

    @Ignore
    @Test
    fun checkSimplePlural0() = pluralTest(
        expected = "zero",
        actual = MR.plurals.test_plural.desc(0)
    )

    @Test
    fun checkSimplePlural1() = pluralTest(
        expected = "one",
        actual = MR.plurals.test_plural.desc(1)
    )

    @Ignore
    @Test
    fun checkSimplePlural2() = pluralTest(
        expected = "two",
        actual = MR.plurals.test_plural.desc(2)
    )

    @Test
    fun checkSimplePlural3() = pluralTest(
        expected = "other",
        actual = MR.plurals.test_plural.desc(3)
    )

    @Test
    fun checkSimplePlural40() = pluralTest(
        expected = "other",
        actual = MR.plurals.test_plural.desc(40)
    )

    @Test
    fun checkSimplePlural22() = pluralTest(
        expected = "other",
        actual = MR.plurals.test_plural.desc(22)
    )

    @Test
    fun checkVariantsPlurals() = pluralTest(
        expected = """
            no items
            1 item
            2 items
            3 items
            4 items
            5 items
            6 items
            7 items
            8 items
            9 items
            10 items
            11 items
            12 items
            13 items
            14 items
            15 items
            16 items
            17 items
            18 items
            19 items
            20 items
            21 items
            22 items
            23 items
            24 items
            25 items
            """.trimIndent(),
        actual = Testing.getPlurals()
    )
}
