/*
 * Copyright 2025 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

import dev.icerock.moko.resources.provider.JsStringProvider

actual data class CompositionStringDesc actual constructor(
    val args: Iterable<StringDesc>,
    val separator: String?
) : StringDesc {

    override suspend fun toLocalizedString(): String = args
        .map { child -> child.toLocalizedString() }
        .joinToString(separator = separator ?: "")

    override fun toLocalizedString(provider: JsStringProvider): String = args
        .joinToString(separator = separator ?: "") { child ->
            child.toLocalizedString(provider)
        }
}
