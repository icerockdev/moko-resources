/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import android.content.Context
import android.os.Parcelable
import androidx.annotation.StringRes
import kotlinx.parcelize.Parcelize

@Parcelize
actual data class StringResource(
    @StringRes val resourceId: Int
) : Parcelable {

    fun getString(context: Context): String = context.getString(resourceId)
}
