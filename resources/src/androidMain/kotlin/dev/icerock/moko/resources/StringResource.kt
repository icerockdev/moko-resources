/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import android.content.Context
import androidx.annotation.StringRes

actual data class StringResource(
    @StringRes val resourceId: Int
) {

    fun getString(context: Context): String = context.getString(resourceId)
}
