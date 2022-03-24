/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

import dev.icerock.moko.resources.StringResource

actual data class ResourceFormattedStringDesc actual constructor(
    private val stringRes: StringResource,
    private val args: List<Any>
) : StringDesc {
    override fun localized() = stringRes.localized(
        locale = StringDesc.localeType.currentLocale,
        *(args.toTypedArray())
    )
}
