package com.icerockdev.desktop

import androidx.compose.desktop.Window
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.imageFromResource
import androidx.compose.ui.text.font.FontListFontFamily
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


fun main() {
    val testing = Testing
    Window("moko-resources", IntSize(1080, 960)) {
        Surface(Modifier.fillMaxSize()) {
            Column {
                Row {
                    testing.getStrings().forEach {
                        Text(
                            it.localized(),
                            color = Color(MR.colors.textColor.color.argb),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
                Text(testing.getStringDesc().localized())
                Text(testing.getTextFile().readText())
                Text(testing.getJsonFile().readText())
                Text(testing.getNestedJsonFile().readText())
                Image(testing.getDrawable().image.toImageBitmap(), Modifier.size(56.dp))
            }
        }
    }
}

// TODO this needs to be updated, would be nice to have a converter in compose
fun BufferedImage.toImageBitmap(): ImageBitmap {
    val byteArray = (raster.dataBuffer as DataBufferByte).data
    val imageInfo = ImageInfo(width, height, ColorType.RGB_565, ColorAlphaType.OPAQUE)
    return Bitmap().apply {
        installPixels(imageInfo, byteArray, imageInfo.minRowBytes)
    }.asImageBitmap()
}
