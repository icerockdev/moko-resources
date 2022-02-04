/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

import dev.icerock.moko.resources.PluralsResource

actual class PluralFormattedStringDesc actual constructor(
    val pluralsRes: PluralsResource,
    val number: Int,
    val args: List<Any>
) : StringDesc {

    override fun localized(): String {
        return pluralsRes.localized(
            locale = StringDesc.localeType.locale,
            quantity = number,
            *args.toTypedArray()
        )
    }
}
