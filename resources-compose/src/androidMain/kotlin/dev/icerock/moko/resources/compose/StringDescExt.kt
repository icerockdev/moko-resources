/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.compose

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import dev.icerock.moko.resources.desc.StringDesc

@Composable
actual fun StringDesc.localized(): String {
    // If recreate context is off
    LocalConfiguration.current
    val context: Context = LocalContext.current
    return toString(context)
}
