/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

import dev.icerock.moko.resources.StringResource

actual class ResourceFormattedStringDesc actual constructor(
    stringRes: StringResource,
    args: List<Any>
) : StringDesc {
    override suspend fun localized(): String = ""
}