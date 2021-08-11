/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.desktop

import androidx.compose.desktop.Window
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.icerockdev.library.MR
import com.icerockdev.library.Testing
import org.jetbrains.skija.Bitmap
import org.jetbrains.skija.ColorAlphaType
import org.jetbrains.skija.ColorType
import org.jetbrains.skija.ImageInfo
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte

import dev.icerock.moko.resourcesCompose.toImageBitmap

fun main() {
    val testing = Testing
    Window("moko-resources", IntSize(1080, 960)) {
        Surface(Modifier.fillMaxSize()) {
            Column {
                Image(
                    bitmap = testing.getDrawable().image.toImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.size(56.dp)
                )
                testing.getStrings().forEach { stringDesc ->
                    Text(
                        text = stringDesc.localized(),
                        color = Color(MR.colors.textColor.color.argb),
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Text(text = testing.getStringDesc().localized())
                Text(text = testing.getTextFile().readText())
                Text(text = testing.getJsonFile().readText())
                Text(text = testing.getNestedJsonFile().readText())
            }
        }
    }
}

