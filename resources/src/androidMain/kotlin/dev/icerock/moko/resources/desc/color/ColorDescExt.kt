/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc.color

import android.content.Context
import android.content.res.Configuration

fun ColorDesc.getColor(context: Context): Int {
    return when (this) {
        is ColorDescResource -> this.resource.getColor(context)
        is ColorDescSingle -> color.argb.toInt()
        is ColorDescThemed -> {
            val configuration: Configuration = context.resources.configuration
            when (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                // Night mode is not active, we're using the light theme
                Configuration.UI_MODE_NIGHT_NO -> lightColor
                // Night mode is active, we're using dark theme
                Configuration.UI_MODE_NIGHT_YES -> darkColor
                // No mode type has been set
                else -> lightColor
            }.argb.toInt()
        }

        else -> throw IllegalArgumentException("unknown class ${this::class}")
    }
}
