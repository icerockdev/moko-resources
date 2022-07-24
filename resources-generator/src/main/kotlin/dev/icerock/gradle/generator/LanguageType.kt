/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import java.util.Locale as JvmLocale

sealed interface LanguageType {

    fun toAndroidResourcesString(): String

    object Base : LanguageType {
        override fun toAndroidResourcesString(): String = "values"
    }

    class Locale(languageTag: String) : LanguageType {

        private val locale: JvmLocale = JvmLocale.forLanguageTag(languageTag)

        fun toBcpString(): String = locale.toLanguageTag()
        fun toLocaleString(): String = locale.toString()
        override fun toAndroidResourcesString(): String = buildString {
            append("values")
            append("-")
            append(locale.language)
            if (locale.country.isNotBlank()) {
                append("-r")
                append(locale.country)
            }
        }

        /**
         * Throw an error here so that we safeguard ourselves from implcitly calling `Local.toString`.
         * You should always use the more explicit methods defined above.
         */
        override fun toString(): String = TODO("Use toLocaleString or toBcpString instead!")

        override fun hashCode(): Int = locale.hashCode()
        override fun equals(other: Any?): Boolean {
            return other is Locale && other.locale == locale
        }
    }

    companion object {

        private const val BASE = "base"

        fun fromFileName(fileName: String): LanguageType = when (fileName) {
            BASE -> Base
            else -> Locale(fileName.replace("-r", "-"))
        }
    }
}
