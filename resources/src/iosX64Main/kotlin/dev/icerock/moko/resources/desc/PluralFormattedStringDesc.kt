/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

import dev.icerock.moko.resources.PluralsResource
import dev.icerock.moko.resources.objc.pluralizedString

actual data class PluralFormattedStringDesc actual constructor(
    val pluralsRes: PluralsResource,
    val number: Int,
    val args: List<Any>
) : StringDesc {
    override fun localized(): String {
        val pluralized = pluralizedString(
            bundle = StringDesc.localeType.getLocaleBundle(pluralsRes.bundle),
            baseBundle = pluralsRes.bundle,
            resourceId = pluralsRes.resourceId,
            number = number
        )!!
        return Utils.stringWithFormat(pluralized, Utils.processArgs(args))
    }
}