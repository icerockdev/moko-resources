/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import android.content.Context
import android.graphics.Typeface
import android.os.Parcelable
import androidx.annotation.FontRes
import androidx.core.content.res.ResourcesCompat
import kotlinx.android.parcel.Parcelize

@Parcelize
actual class FontResource(
    @FontRes
    private val fontResourceId: Int
) : Parcelable {

    fun getTypeface(context: Context): Typeface? {
        return ResourcesCompat.getFont(context, fontResourceId)
    }
}
