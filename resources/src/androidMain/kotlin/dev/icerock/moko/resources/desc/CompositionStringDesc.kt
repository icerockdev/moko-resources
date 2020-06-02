/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

import android.content.Context

actual data class CompositionStringDesc actual constructor(
    val args: List<StringDesc>,
    val separator: String?
) : StringDesc {
    override fun toString(context: Context): String {
        return StringBuilder().apply {
            args.forEachIndexed { index, stringDesc ->
                if (index != 0 && separator != null) {
                    append(separator)
                }
                append(stringDesc.toString(context))
            }
        }.toString()
    }
}
