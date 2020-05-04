/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

import dev.icerock.moko.resources.PluralsResource

expect class PluralFormattedStringDesc(pluralsRes: PluralsResource, number: Int, args: List<Any>) : StringDesc

@Suppress("FunctionName")
fun StringDesc.Companion.PluralFormatted(
    pluralsRes: PluralsResource,
    number: Int,
    args: List<Any>
) = PluralFormattedStringDesc(pluralsRes, number, args)

@Suppress("FunctionName")
fun StringDesc.Companion.PluralFormatted(
    pluralsRes: PluralsResource,
    number: Int,
    vararg args: Any
) = PluralFormattedStringDesc(pluralsRes, number, args.asList())
