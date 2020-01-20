package com.icerockdev

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.filters.LargeTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.rule.ActivityTestRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
@LargeTest
class StringResTest {

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    private val baseStringsAndPlurals =
        "test\ntest 2\ntest 3\nTest Project\nsome raw string\nother\none\nother\nother"
    private val baseStringDescs =
        "test\ntest\nTest data 9\nother\nother\n10 items\nraw string\nraw string\ntestraw string" +
                "\nCHOOSE PORTFOLIO & AMOUNT\nsecond string str first decimal 9"

    @Before
    fun initValidString() {
    }

    @Test
    fun baseStringsTest() {
        onView(withId(R.id.textView)).check(matches(withText(baseStringsAndPlurals)))
    }

    @Test
    fun stringDescsTest() {
        onView(withId(R.id.stringDescTextView)).check(matches(withText(baseStringDescs)))
    }

}
