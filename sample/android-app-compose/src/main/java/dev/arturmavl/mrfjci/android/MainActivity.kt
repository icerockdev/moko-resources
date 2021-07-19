package dev.arturmavl.mrfjci.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp


internal class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}

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