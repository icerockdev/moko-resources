/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.resources.PluralsResource

actual data class PluralStringDesc actual constructor(
    val pluralsRes: PluralsResource,
    val number: Int
) : StringDesc, Parcelable {
    override fun localized(): String {
        return pluralizedString(
            bundle = StringDesc.localeType.getLocaleBundle(pluralsRes.bundle),
            baseBundle = pluralsRes.bundle,
            locale = StringDesc.localeType.locale,
            resourceId = pluralsRes.resourceId,
            number = number
        )
    }
}
