/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

actual interface StringDesc {

    suspend fun localized(): String

    actual sealed class LocaleType {
        abstract val locale: String?

        actual object System : LocaleType() {
            override val locale: String? = null
        }

        actual class Custom actual constructor(override val locale: String) :
            LocaleType() {
        }
    }

    actual companion object {
        actual var localeType: LocaleType = LocaleType.System
    }
}