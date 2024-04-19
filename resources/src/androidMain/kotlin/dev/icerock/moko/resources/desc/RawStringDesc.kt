/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

import android.content.Context

actual data class RawStringDesc actual constructor(val string: String) : StringDesc {
    override fun toString(context: Context) = string
}
