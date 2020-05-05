/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

import android.content.Context
import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import dev.icerock.moko.resources.PluralsResource

@Parcelize
actual class PluralStringDesc actual constructor(
    val pluralsRes: PluralsResource,
    val number: Int
) : StringDesc, Parcelable {
    override fun toString(context: Context): String {
        return Utils.resourcesForContext(context).getQuantityString(pluralsRes.resourceId, number)
    }
}
