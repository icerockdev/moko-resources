/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

import android.content.Context
import java.util.Locale

actual interface StringDesc {
    fun toString(context: Context): String

    actual sealed class LocaleType {
        abstract val systemLocale: Locale?

        actual object System : LocaleType() {
            override val systemLocale: Locale? = null
        }

        actual class Custom actual constructor(
            languageTag: String
        ) : LocaleType() {
            override val systemLocale: Locale = Locale.forLanguageTag(languageTag)
        }
    }

    actual companion object {
        actual var localeType: LocaleType = LocaleType.System
    }
}
