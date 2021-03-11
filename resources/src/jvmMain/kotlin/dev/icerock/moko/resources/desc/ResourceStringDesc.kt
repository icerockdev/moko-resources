/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.resources.StringResource
import java.util.Locale

actual class ResourceStringDesc actual constructor(
    val stringRes: StringResource
) : StringDesc, Parcelable {

    override fun localized(): String = stringRes.localized(locale = Locale.getDefault())
}
