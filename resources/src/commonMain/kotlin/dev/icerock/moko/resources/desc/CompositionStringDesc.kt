/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

@Suppress("FunctionName")
fun StringDesc.Companion.Composition(
    args: Iterable<StringDesc>,
    separator: String? = null
) = CompositionStringDesc(args, separator)

expect class CompositionStringDesc(
    args: Iterable<StringDesc>,
    separator: String? = null
) : StringDesc
