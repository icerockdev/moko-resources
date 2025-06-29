/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("Filename")

package dev.icerock.moko.resources.compose.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState

@Composable
internal fun produceByteArray(url: String): State<ByteArray?> {
    return produceState(null, url) {
        @Suppress("TooGenericExceptionCaught")
        try {
            value = fetchByteArray(url)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
