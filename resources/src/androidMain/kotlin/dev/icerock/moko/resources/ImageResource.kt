/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import kotlinx.parcelize.Parcelize

@Parcelize
actual data class ImageResource(
    @DrawableRes val drawableResId: Int
) : Parcelable {

    fun getDrawable(context: Context): Drawable? = ContextCompat.getDrawable(context, drawableResId)
}
