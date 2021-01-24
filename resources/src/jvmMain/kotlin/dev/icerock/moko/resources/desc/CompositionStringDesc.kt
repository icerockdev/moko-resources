/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

actual class CompositionStringDesc actual constructor(
    private val args: Iterable<StringDesc>,
    private val separator: String?
) : StringDesc {

    override fun localized() = args.joinToString(separator = separator ?: "") { it.localized() }
}