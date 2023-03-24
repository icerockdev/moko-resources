/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.compose.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.w3c.fetch.Response

@Composable
internal fun produceByteArray(url: String): State<ByteArray?> {
    return produceState<ByteArray?>(null) {
        val response: Response = window.fetch(url).await()

        if (response.ok.not()) {
            console.error("can't load data from $url")
            return@produceState
        }

        val buffer: ArrayBuffer = response.arrayBuffer().await()

        value = Int8Array(buffer)
            .unsafeCast<ByteArray>()
    }
}
