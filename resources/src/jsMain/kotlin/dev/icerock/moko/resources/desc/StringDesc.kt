/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

actual interface StringDesc {
    actual sealed class LocaleType {
        actual object System : LocaleType()
        actual class Custom actual constructor(locale: String) :
            LocaleType()
    }

    actual companion object {
        actual var localeType: LocaleType
            get() = TODO("Not yet implemented")
            set(value) {}
    }

}