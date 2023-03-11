/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.desktop

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication
import com.icerockdev.library.MR
import com.icerockdev.library.Testing
import dev.icerock.moko.resources.compose.toColor
import dev.icerock.moko.resources.compose.toComposeFont

fun main() {
    val testing = Testing

    testing.getTextsFromAssets().forEach {
        println(it.readText())
    }

    singleWindowApplication(
        title = "moko-resources",
        state = WindowState(size = DpSize(1080.dp, 960.dp))
    ) {
        Surface(Modifier.fillMaxSize()) {
            Column {
                Image(
                    bitmap = testing.getDrawable().image.toComposeImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.size(56.dp)
                )
                testing.getStrings().forEach { stringDesc ->
                    Text(
                        text = stringDesc.localized(),
                        color = MR.colors.textColor.toColor(),
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Text(
                    text = testing.getStringDesc().localized(),
                    fontFamily = FontFamily(testing.getFontTtf1().toComposeFont())
                )
                Text(
                    text = testing.getTextFile().readText(),
                    fontFamily = FontFamily(testing.getFontOtf1().toComposeFont())
                )
                Text(text = testing.getJsonFile().readText())
                Text(text = testing.getNestedJsonFile().readText())
            }
        }
    }
}
