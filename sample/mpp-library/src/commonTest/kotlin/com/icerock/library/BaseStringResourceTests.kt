/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("IllegalIdentifier")

package com.icerock.library

import BaseUnitTest
import com.icerockdev.library.MR
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.download
import getString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

abstract class BaseStringResourceTests(
    private val locale: String
) : BaseUnitTest() {

    @BeforeTest
    fun setup() {
        StringDesc.localeType = StringDesc.LocaleType.Custom(locale)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    protected fun stringTest(expected: String, actual: StringDesc) = runTest {
        MR.strings.download()

        assertEquals(
            expected = expected,
            actual = actual.getString()
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    protected fun pluralTest(expected: String, actual: StringDesc) = runTest {
        MR.plurals.download()

        assertEquals(
            expected = expected,
            actual = actual.getString()
        )
    }
}
