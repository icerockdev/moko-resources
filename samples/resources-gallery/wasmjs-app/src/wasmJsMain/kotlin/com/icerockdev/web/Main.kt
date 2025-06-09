/*
 * Copyright 2025 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.web

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeViewport
import com.icerockdev.library.MR
import com.icerockdev.library.Testing
import dev.icerock.moko.resources.compose.painterResource
import kotlinx.browser.document
import kotlinx.browser.window
import dev.icerock.moko.graphics.Color as MokoColor

@OptIn(ExperimentalComposeUiApi::class)
suspend fun main() {
    val strings = MR.stringsLoader.getOrLoad()

    ComposeViewport(document.body!!) {
        MaterialTheme {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = Testing.getStringDesc().toLocalizedString(strings),
                )

                Spacer(Modifier.height(16.dp))

                Image(
                    painter = painterResource(MR.images.car_black),
                    contentDescription = null,
                )

                Spacer(Modifier.height(16.dp))

                val color by remember(window) {
                    Testing.getGradientColors().first().getColorFlow(window)
                }.collectAsState(initial = MokoColor(0xFFAAFFFF))

                val (r, g, b, a) = color
                Text(
                    text = "Color Test",
                    style = TextStyle(
                        color = Color(r, g, b, a)
                    )
                )
            }
        }
    }
}
