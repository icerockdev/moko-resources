/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import android.content.Context
import android.content.res.Configuration
import dev.icerock.moko.graphics.Color

fun ColorResource.getColor(context: Context): Color {
    return when (this) {
        is ColorResource.Single -> color
        is ColorResource.Themed -> {
            val configuration = context.resources.configuration
            when (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_NO -> light // Night mode is not active, we're using the light theme
                Configuration.UI_MODE_NIGHT_YES -> dark // Night mode is active, we're using dark theme
                else -> light // No mode type has been set
            }
        }
    }
}
