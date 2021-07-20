/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */
package com.icerockdev

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.icerockdev.library.MR
import dev.icerock.moko.resources.FontResource


internal class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}

private val MontserratRegular: FontResource = MR.fonts.Raleway.bold

private val monserratFontFamily: FontFamily = FontFamily(
    fonts = listOf(
        Font(
            resId = MontserratRegular.fontResourceId,
            weight = FontWeight.Normal,
            style = FontStyle.Normal
        )
    )
)

internal val Typography: Typography = Typography(
    body1 = TextStyle(
        fontFamily = monserratFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    )
)