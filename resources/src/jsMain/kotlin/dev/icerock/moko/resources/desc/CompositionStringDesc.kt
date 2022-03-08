/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

import dev.icerock.moko.resources.provider.JsStringProvider

actual class CompositionStringDesc actual constructor(
    val args: Iterable<StringDesc>,
    val separator: String?
) : StringDesc {
    override suspend fun localized(): String = args
        .map { child -> child.localized() }
        .joinToString(separator = separator ?: "")

    override fun localized(provider: JsStringProvider): String = args
        .joinToString(separator = separator ?: "") { child ->
            child.localized(provider)
        }
}
