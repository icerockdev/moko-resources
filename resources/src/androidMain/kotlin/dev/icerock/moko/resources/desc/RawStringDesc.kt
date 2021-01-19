/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

import android.content.Context
import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize

@Parcelize
actual data class RawStringDesc actual constructor(
    private val string: String
) : StringDesc, Parcelable {
    override fun toString(context: Context) = string
}
