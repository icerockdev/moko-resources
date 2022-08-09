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
            private val locale: String
        ) : LocaleType() {
            override val systemLocale: Locale get() {
                val languageTagParts = locale.split("-")
                return when (languageTagParts.size) {
                    LANGUAGE -> Locale(languageTagParts[0])
                    LANGUAGE_AND_COUNTRY -> Locale(languageTagParts[0], languageTagParts[1])
                    LANGUAGE_AND_COUNTRY_AND_VARIANT -> Locale(
                        languageTagParts[0],
                        languageTagParts[1],
                        languageTagParts[2]
                    )
                    else -> throw IllegalArgumentException(
                        "Invalid language tag $locale which has more than three parts."
                    )
                }
            }
        }
    }

    actual companion object {
        actual var localeType: LocaleType = LocaleType.System
    }
}

private const val LANGUAGE = 1
private const val LANGUAGE_AND_COUNTRY = 2
private const val LANGUAGE_AND_COUNTRY_AND_VARIANT = 3
