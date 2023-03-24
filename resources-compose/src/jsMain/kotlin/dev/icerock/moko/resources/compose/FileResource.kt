/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import dev.icerock.moko.resources.FileResource

@Composable
actual fun FileResource.readTextAsState(): State<String?> {
    return produceState<String?>(null) {
        value = getText()
    }
}
