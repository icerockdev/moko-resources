/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

import dev.icerock.moko.resources.PluralsResource
import dev.icerock.moko.resources.StringResource

fun Int.strResDesc() = StringResource(this).desc()
fun Int.plrResDesc(number: Int) = PluralsResource(this).desc(number)
