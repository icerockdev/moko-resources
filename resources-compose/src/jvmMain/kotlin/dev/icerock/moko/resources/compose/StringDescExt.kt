/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.compose

import androidx.compose.runtime.Composable
import dev.icerock.moko.resources.desc.StringDesc

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun StringDesc.localized(): String {
    return this.localized()
}
