/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev

import android.preference.PreferenceManager
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import org.junit.Rule
import org.junit.Test

abstract class StringResourcesTest {

    abstract val stringAndPluralsCheck: String
    abstract val stringDescsCheck: String

    abstract val locale: String

    @get:Rule
    val activityRule = object : ActivityTestRule<MainActivity>(MainActivity::class.java) {
        override fun beforeActivityLaunched() {
            val appContext = InstrumentationRegistry
                .getInstrumentation()
                .targetContext
                .applicationContext

            PreferenceManager.getDefaultSharedPreferences(appContext)
                .edit()
                .putString(LocaleHandler.LOCALE_PREF_TAG, locale)
                .commit()
        }
    }

    @Test
    fun testStringAndPlurals() {
        onView(ViewMatchers.withId(R.id.textView))
            .check(ViewAssertions.matches(ViewMatchers.withText(stringAndPluralsCheck)))
    }

    @Test
    fun testStringDescs() {
        onView(ViewMatchers.withId(R.id.stringDescTextView))
            .check(ViewAssertions.matches(ViewMatchers.withText(stringDescsCheck)))
    }
}
