/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

import dev.icerock.moko.resources.PluralsResource
import kotlinx.cinterop.BetaInteropApi
import platform.Foundation.NSBundle
import platform.Foundation.NSLocale
import platform.Foundation.NSString
import platform.Foundation.create

actual data class PluralFormattedStringDesc actual constructor(
    val pluralsRes: PluralsResource,
    val number: Int,
    val args: List<Any>
) : StringDesc {
    override fun localized(): String {
        val pluralized = pluralizedString(
            bundle = StringDesc.localeType.getLocaleBundle(pluralsRes.bundle),
            baseBundle = pluralsRes.bundle,
            locale = StringDesc.localeType.locale,
            resourceId = pluralsRes.resourceId,
            number = number
        )
        return Utils.stringWithFormat(
            pluralized,
            Utils.processArgs(args)
        )
    }
}

@OptIn(BetaInteropApi::class)
internal fun pluralizedString(
    bundle: NSBundle,
    baseBundle: NSBundle,
    locale: NSLocale,
    resourceId: String,
    number: Int
): String {
    val localized = bundle
        .localizedStringForKey(resourceId, null, null)
        .takeUnless { it == resourceId }
        ?: baseBundle.localizedStringForKey(resourceId, null, null)
    @Suppress("CAST_NEVER_SUCCEEDS")
    return NSString.create(
        format = localized,
        locale = locale,
        args = arrayOf(number)
    ) as String
}
