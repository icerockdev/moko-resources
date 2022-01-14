/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

import java.util.Locale

actual interface StringDesc {
    fun localized(): String

    actual sealed class LocaleType {
        abstract val currentLocale: Locale

        actual object System : LocaleType() {
            override val currentLocale: Locale get() = Locale.getDefault()
        }

        actual class Custom actual constructor(
            private val locale: String
        ) : LocaleType() {
            override val currentLocale: Locale get() = Locale(locale)
        }
    }

    actual companion object {
        actual var localeType: LocaleType = LocaleType.System
    }
}
