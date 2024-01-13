/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package me.amikhailov.common

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication
import com.icerockdev.app.MR

fun main() {
    singleWindowApplication(
        title = MR.strings.title.localized(),
        state = WindowState(size = DpSize(1080.dp, 960.dp))
    ) {
        Surface(Modifier.fillMaxSize()) {
            App()
        }
    }
}
