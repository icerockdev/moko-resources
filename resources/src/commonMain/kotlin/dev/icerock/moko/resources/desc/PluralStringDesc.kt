/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.resources.PluralsResource

@Suppress("FunctionName")
fun StringDesc.Companion.Plural(pluralsRes: PluralsResource, number: Int) = PluralStringDesc(pluralsRes, number)

expect class PluralStringDesc(pluralsRes: PluralsResource, number: Int) : StringDesc, Parcelable
