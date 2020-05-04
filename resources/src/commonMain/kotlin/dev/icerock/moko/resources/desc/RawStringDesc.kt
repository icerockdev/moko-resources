/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

import dev.icerock.moko.parcelize.Parcelable

expect class RawStringDesc(string: String) : StringDesc, Parcelable

@Suppress("FunctionName")
fun StringDesc.Companion.Raw(string: String) = RawStringDesc(string)
