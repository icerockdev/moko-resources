/*
 * Copyright 2026 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.myapplication.common

import androidx.compose.material.Text
import app.cash.paparazzi.Paparazzi
import com.icerockdev.library.MR
import com.icerockdev.library.hello_world
import dev.icerock.moko.resources.compose.stringResource
import org.junit.Rule
import org.junit.Test

class MokoResourcesPaparazziTest {
    @get:Rule
    val paparazzi = Paparazzi()

    @Test
    fun rendersGeneratedStringResource() {
        paparazzi.snapshot {
            Text(text = stringResource(MR.strings.hello_world))
        }
    }
}
