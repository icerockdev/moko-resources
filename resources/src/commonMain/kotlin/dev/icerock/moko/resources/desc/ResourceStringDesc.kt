/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.resources.StringResource

@Suppress("FunctionName")
fun StringDesc.Companion.Resource(stringRes: StringResource) = ResourceStringDesc(stringRes)

expect class ResourceStringDesc(stringRes: StringResource) : StringDesc, Parcelable
