/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import dev.icerock.moko.resources.desc.PluralFormatted
import dev.icerock.moko.resources.desc.StringDesc

expect class PluralsResource

fun PluralsResource.format(number: Int, vararg args: Any) = StringDesc.PluralFormatted(this, number, args)
fun PluralsResource.format(number: Int, args: List<Any>) = StringDesc.PluralFormatted(this, number, args)
