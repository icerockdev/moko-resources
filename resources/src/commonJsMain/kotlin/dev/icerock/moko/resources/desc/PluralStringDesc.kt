/*
 * Copyright 2025 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

import dev.icerock.moko.resources.PluralsResource
import dev.icerock.moko.resources.provider.JsStringProvider

actual data class PluralStringDesc actual constructor(
    val pluralsRes: PluralsResource,
    val number: Int,
) : StringDesc {

    override suspend fun toLocalizedString(): String =
        toLocalizedString(pluralsRes.loader.getOrLoad())

    override fun toLocalizedString(provider: JsStringProvider): String {
        return pluralsRes.localized(
            provider = provider,
            locale = StringDesc.localeType.locale,
            quantity = number
        )
    }
}
