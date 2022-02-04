/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("IllegalIdentifier")

package com.icerock.library

import com.icerockdev.library.MR
import com.icerockdev.library.Testing
import dev.icerock.moko.resources.desc.desc
import kotlin.test.Ignore
import kotlin.test.Test

class PluralResourceRuTests : BaseStringResourceTests("ru") {

    @Ignore
    @Test
    fun checkSimplePlural0() = pluralTest(
        expected = "ноль",
        actual = MR.plurals.test_plural.desc(0)
    )

    @Test
    fun checkSimplePlural1() = pluralTest(
        expected = "один",
        actual = MR.plurals.test_plural.desc(1)
    )

    @Ignore
    @Test
    fun checkSimplePlural2() = pluralTest(
        expected = "два",
        actual = MR.plurals.test_plural.desc(2)
    )

    @Test
    fun checkSimplePlural3() = pluralTest(
        expected = "несколько",
        actual = MR.plurals.test_plural.desc(3)
    )

    @Test
    fun checkSimplePlural40() = pluralTest(
        expected = "много",
        actual = MR.plurals.test_plural.desc(40)
    )

    @Test
    fun checkSimplePlural22() = pluralTest(
        expected = "несколько",
        actual = MR.plurals.test_plural.desc(22)
    )

    @Test
    fun checkVariantsPlurals() = pluralTest(
        expected = """
            1 элемент
            2 элемента
            3 элемента
            4 элемента
            5 элементов
            6 элементов
            7 элементов
            8 элементов
            9 элементов
            10 элементов
            11 элементов
            12 элементов
            13 элементов
            14 элементов
            15 элементов
            16 элементов
            17 элементов
            18 элементов
            19 элементов
            20 элементов
            21 элемент
            22 элемента
            23 элемента
            24 элемента
            25 элементов
            26 элементов
            """.trimIndent(),
        actual = Testing.getPlurals()
    )
}
