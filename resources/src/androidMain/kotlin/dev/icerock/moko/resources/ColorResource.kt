/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import android.content.Context
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

actual class ColorResource(
    @ColorRes val resourceId: Int
) {
    fun getColor(context: Context): Int = ContextCompat.getColor(context, resourceId)
}
