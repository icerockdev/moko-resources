/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

import android.content.Context
import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import dev.icerock.moko.resources.StringResource

@Parcelize
actual data class ResourceStringDesc actual constructor(
    val stringRes: StringResource
) : StringDesc, Parcelable {
    override fun toString(context: Context): String {
        return Utils.resourcesForContext(context).getString(stringRes.resourceId)
    }
}
