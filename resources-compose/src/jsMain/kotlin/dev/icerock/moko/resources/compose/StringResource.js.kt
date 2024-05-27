/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import dev.icerock.moko.resources.desc.StringDesc

@Composable
internal actual fun localized(stringDesc: StringDesc): String {
    return produceState(initialValue = "", stringDesc) {
        value = stringDesc.toLocalizedString()
    }.value
}
