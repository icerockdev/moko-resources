/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.resources.StringResource

actual data class ResourceStringDesc actual constructor(
    private val stringRes: StringResource
) : StringDesc, Parcelable {
    override fun localized(): String {
        return Utils.localizedString(stringRes)
    }
}