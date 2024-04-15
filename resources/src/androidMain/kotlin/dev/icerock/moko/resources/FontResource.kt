/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import android.content.Context
import android.graphics.Typeface
import androidx.annotation.FontRes
import androidx.core.content.res.ResourcesCompat

actual class FontResource(
    @FontRes
    val fontResourceId: Int
)  {

    fun getTypeface(context: Context): Typeface? {
        return ResourcesCompat.getFont(context, fontResourceId)
    }
}
