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
            private val languageTag: String
        ) : LocaleType() {
            override val currentLocale: Locale get() {
                val languageTagParts = languageTag.split("-")
                return when (languageTagParts.size) {
                    1 -> Locale(languageTagParts[0])
                    2 -> Locale(languageTagParts[0], languageTagParts[1])
                    3 -> Locale(languageTagParts[0], languageTagParts[1], languageTagParts[2])
                    else -> throw IllegalArgumentException("Invalid language tag $languageTag which has more than three parts.")
                }
            }
        }
    }

    actual companion object {
        actual var localeType: LocaleType = LocaleType.System
    }
}
