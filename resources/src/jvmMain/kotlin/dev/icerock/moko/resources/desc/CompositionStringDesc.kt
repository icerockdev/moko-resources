/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

actual class CompositionStringDesc actual constructor(
    val args: Iterable<StringDesc>,
    val separator: String?
) : StringDesc {

    override fun localized() = args.joinToString(separator = separator ?: "") { it.localized() }
}
