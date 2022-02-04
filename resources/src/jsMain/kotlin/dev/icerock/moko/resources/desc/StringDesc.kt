/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

import dev.icerock.moko.resources.internal.ResourcesNotLoaded
import dev.icerock.moko.resources.internal.currentLocale

actual interface StringDesc {

    fun localized(): String

    actual sealed class LocaleType {
        abstract val locale: String?

        actual object System : LocaleType() {
            override val locale: String? get() = currentLocale()
        }

        actual class Custom actual constructor(override val locale: String) : LocaleType()
    }

    actual companion object {
        actual var localeType: LocaleType = LocaleType.System
    }
}

suspend fun StringDesc.localizedAsync(): String {
    return try {
        localized()
    } catch (exc: ResourcesNotLoaded) {
        // at now resources not downloaded to client side. download it and retry
        exc.download()
        localized()
    }
}
