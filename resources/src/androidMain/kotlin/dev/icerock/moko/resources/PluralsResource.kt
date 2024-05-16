/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import android.content.Context
import androidx.annotation.PluralsRes

actual class PluralsResource(
    @PluralsRes val resourceId: Int
) {

    fun getQuantityString(context: Context, number: Int): String {
        return context.resources.getQuantityString(resourceId, number)
    }
}
