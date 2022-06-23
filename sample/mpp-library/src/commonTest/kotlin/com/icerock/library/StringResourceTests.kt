/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerock.library

import BaseUnitTest
import com.icerockdev.library.MR
import com.icerockdev.library.Testing
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import dev.icerock.moko.resources.format
import getString
import kotlinx.coroutines.test.runTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

class StringResourceTests : BaseUnitTest() {

    @Test
    fun compareStrings() {
        assertEquals(Testing.getStringDesc(), Testing.getStringDesc())
        assertEquals(MR.strings.format.format(2), MR.strings.format.format(2))
    }

    @Test
    fun checkRuStrings() = runTest {
        StringDesc.localeType = StringDesc.LocaleType.Custom("ru")
        val rawString: String = Testing.getStringDesc().getString()

        assertEquals(
            expected = "тест\nтест\nТестовые данные 9\nмного\nмного\n10 элементов\n10 элементов\n" +
                    "raw string\nraw string\nтестraw string\nВыберите портфель и сумму\n" +
                    "вторая строка str первое число 9",
            actual = rawString
        )
    }

    @Test
    fun checkEnStrings() = runTest {
        StringDesc.localeType = StringDesc.LocaleType.Custom("en")
        val rawString: String = Testing.getStringDesc().getString()

        assertEquals(
            expected = "test\ntest\nTest data 9\nother\nother\n10 items\n10 items\nraw string\n" +
                    "raw string\ntestraw string\nCHOOSE PORTFOLIO & AMOUNT\n" +
                    "second string str first decimal 9",
            actual = rawString
        )
    }

    @Test
    fun checkEnString() = runTest {
        StringDesc.localeType = StringDesc.LocaleType.Custom("en")
        val rawString: String = MR.strings.test_simple.desc().getString()
        assertEquals(
            expected = "test",
            actual = rawString
        )
    }

    @Test
    fun checkEnUsString() = runTest {
        StringDesc.localeType = StringDesc.LocaleType.Custom("en-US")
        val rawString: String = MR.strings.test_simple.desc().getString()
        assertEquals(
            expected = "test US",
            actual = rawString
        )
    }

    @Test
    fun checkEnGbString() = runTest {
        StringDesc.localeType = StringDesc.LocaleType.Custom("en-GB")
        val rawString: String = MR.strings.test_simple.desc().getString()
        assertEquals(
            expected = "test UK",
            actual = rawString
        )
    }

    @Ignore
    @Test
    fun checkRuStringsList() = runTest {
        StringDesc.localeType = StringDesc.LocaleType.Custom("ru")
        val rawString: String = Testing.getStrings().map { it.getString() }
            .joinToString("\n")

        assertEquals(
            expected = "тест\nтест 2\nтест 3\nТестовый проект\nsome raw string\nодин\n" +
                    "несколько\nнесколько\nпервая строка\nвторая строка\nтретья строка.\n" +
                    "Alex009 сказал \"привет мир\" & \"пишите тесты\".\nтест вложенный\nмного\n" +
                    "plurals-interop: один\nplurals-interop: несколько\nplurals-interop: много",
            actual = rawString
        )
    }

    @Ignore
    @Test
    fun checkEnStringsList() = runTest {
        StringDesc.localeType = StringDesc.LocaleType.Custom("en")
        val rawString: String = Testing.getStrings().map { it.getString() }
            .joinToString("\n")

        assertEquals(
            expected = "test\ntest 2\ntest 3\nTest Project\nsome raw string\none\nother\n" +
                    "other\nfirst line\nsecond line\nthird line.\n" +
                    "Alex009 said \"hello world\" & \"write tests\".\nnested test\nother\n" +
                    "plurals-interop: one\nplurals-interop: other\nplurals-interop: other",
            actual = rawString
        )
    }
}
