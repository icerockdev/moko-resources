/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

import dev.icerock.moko.resources.StringResource

@Suppress("FunctionName")
fun StringDesc.Companion.ResourceFormatted(
    stringRes: StringResource,
    args: List<Any>
) = ResourceFormattedStringDesc(stringRes, args)

@Suppress("FunctionName")
fun StringDesc.Companion.ResourceFormatted(
    stringRes: StringResource,
    vararg args: Any
) = ResourceFormattedStringDesc(stringRes, args.asList())

expect class ResourceFormattedStringDesc(stringRes: StringResource, args: List<Any>) : StringDesc