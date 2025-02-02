/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

import dev.icerock.moko.resources.PluralsResource

actual data class PluralFormattedStringDesc actual constructor(
    val pluralsRes: PluralsResource,
    val number: Int,
    val args: List<Any>
) : StringDesc {
    @Suppress("SpreadOperator")
    override fun localized() = pluralsRes.localized(
        locale = StringDesc.localeType.currentLocale,
        quantity = number,
        *Utils.processArgs(args)
    )
}
