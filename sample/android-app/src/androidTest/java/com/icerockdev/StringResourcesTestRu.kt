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

    override val stringDescsCheck: String
        get() = "тест\nтест\nТестовые данные 9\nмного\nмного\n10 элементов\nraw string\nraw string" +
                "\nтестraw string\nCHOOSE PORTFOLIO & AMOUNT\nsecond string str first decimal 9"

}
