/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

import dev.icerock.moko.resources.internal.currentLocale
import dev.icerock.moko.resources.provider.JsStringProvider

actual interface StringDesc {
    suspend fun localized(): String
    fun localized(provider: JsStringProvider): String

    actual sealed class LocaleType {
        abstract val locale: String

        actual object System : LocaleType() {
            override val locale: String get() = currentLocale()
        }

        actual class Custom actual constructor(override val locale: String) : LocaleType()
    }

    actual companion object {
        actual var localeType: LocaleType = LocaleType.System
    }
}
