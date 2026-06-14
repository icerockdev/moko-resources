/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerock.library

import app.gradle9sample.library.MR
import app.gradle9sample.library.test_plural
import app.gradle9sample.library.test_plural_fallback
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
    fun checkPluralFallback1() = pluralTest(
        expected = "one",
        actual = MR.plurals.test_plural_fallback.desc(1)
    )

    @Test
    fun checkPluralFallback2() = pluralTest(
        expected = "other",
        actual = MR.plurals.test_plural_fallback.desc(2)
    )
}
