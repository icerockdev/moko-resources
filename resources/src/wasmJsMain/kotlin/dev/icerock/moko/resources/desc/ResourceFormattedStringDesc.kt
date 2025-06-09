/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.provider.JsStringProvider

actual data class ResourceFormattedStringDesc actual constructor(
    val stringRes: StringResource,
    val args: List<Any>
) : StringDesc {
    override suspend fun toLocalizedString(): String =
        toLocalizedString(stringRes.loader.getOrLoad())

    override fun toLocalizedString(provider: JsStringProvider): String {
        return stringRes.localized(
            provider = provider,
            locale = StringDesc.localeType.locale,
            *Utils.processArgs(args, provider)
        )
    }
}
