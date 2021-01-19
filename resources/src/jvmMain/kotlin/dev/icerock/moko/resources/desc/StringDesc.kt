package dev.icerock.moko.resources.desc

import java.util.*

actual interface StringDesc {
    fun localized(): String

    actual sealed class LocaleType {
        abstract val systemLocale: Locale?

        actual object System : LocaleType() {
            override val systemLocale: Locale? = null
        }

        actual class Custom actual constructor(
            locale: String
        ) : LocaleType() {
            override val systemLocale: Locale = Locale(locale)
        }

    }

    actual companion object {
        actual var localeType: LocaleType = LocaleType.System
    }

}