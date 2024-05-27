/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

import dev.icerock.moko.resources.provider.JsStringProvider

actual data class RawStringDesc actual constructor(
    val string: String
) : StringDesc {
    override suspend fun toLocalizedString(): String = string
    override fun toLocalizedString(provider: JsStringProvider): String = string
}
