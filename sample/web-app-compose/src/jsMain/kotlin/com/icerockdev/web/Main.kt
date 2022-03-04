/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.web

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.icerockdev.library.MR
import com.icerockdev.library.Testing
import dev.icerock.moko.graphics.Color
import dev.icerock.moko.resources.ColorResource
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.renderComposable

suspend fun main() {
    val strings = MR.strings.stringsLoader.getOrLoad() + MR.plurals.stringsLoader.getOrLoad()

    val fileText = mutableStateOf("")

    renderComposable(rootElementId = "root") {
        val rememberFileText = remember { fileText }

        Div(
            attrs = {
                style {
                    display(DisplayStyle.Flex)
                    flexDirection(FlexDirection.Column)
                }
            }
        ) {
            Span {
                P(
                    attrs = {
//                        style {
//                            fontFamily(Testing.getFontTtf1().fileUrl)
//                        }
                    }
                ) {
                    Text(Testing.getStringDesc().localized(strings))
                }
                Br()
                P(
                    attrs = {
                        style {
                            val (r, g, b, a) =
                                (Testing.getGradientColors().first() as ColorResource.Single).color
                            color(rgba(r, g, b, a))
                        }
                    }
                ) {
                    Text("Color Test")
                }
                Br()
                Img(src = Testing.getDrawable().fileUrl)
                Br()
                Text(
                    value = rememberFileText.value
                )
            }
        }


    }

    coroutineScope {
        launch {
            fileText.value = Testing.getTextFile().getText()
        }
    }
}
