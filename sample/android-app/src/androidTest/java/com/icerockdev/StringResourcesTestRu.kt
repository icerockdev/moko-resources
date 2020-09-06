/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev

import androidx.test.filters.LargeTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
@LargeTest
class StringResourcesTestRu : StringResourcesTest() {

    override val locale: String = "ru"

    override val stringAndPluralsCheck: String
        get() = "тест\nтест 2\nтест 3\nТестовый проект\nsome raw string\nмного\nодин\nнесколько\nнесколько"
    // commented while not fix https://youtrack.jetbrains.com/issue/KT-41384
    // \nтест вложенный"

    override val stringDescsCheck: String
        get() = "тест\nтест\nТестовые данные 9\nмного\nмного\n10 элементов\nraw string\nraw string\n" +
                "тестraw string\nВыберите портфель и сумму\nвторая строка str первое число 9"
}
