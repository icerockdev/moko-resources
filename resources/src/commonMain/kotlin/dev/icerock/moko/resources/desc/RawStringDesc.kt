/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

@Suppress("FunctionName")
fun StringDesc.Companion.Raw(string: String) = RawStringDesc(string)

expect class RawStringDesc(string: String) : StringDesc
