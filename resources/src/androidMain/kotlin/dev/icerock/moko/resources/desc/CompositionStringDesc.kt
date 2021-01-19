/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

import android.content.Context

actual data class CompositionStringDesc actual constructor(
    private val args: Iterable<StringDesc>,
    private val separator: String?
) : StringDesc {
    override fun toString(context: Context) =
        args.joinToString(separator = separator ?: "") { it.toString(context) }
}
