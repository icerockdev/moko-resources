/*
 * Copyright 2025 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.compose.internal

import kotlinx.browser.window
import kotlinx.coroutines.await
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.get
import org.w3c.fetch.Response

internal actual suspend fun fetchByteArray(url: String): ByteArray {
    val response: Response = window.fetch(url).await()

    if (response.ok.not()) {
        error("can't load data from $url : $response")
    }

    val buffer: ArrayBuffer = response.arrayBuffer().await()

    return Int8Array(buffer).toByteArray()
}

private fun Int8Array.copyInto(inputOffset: Int, output: ByteArray, outputOffset: Int, length: Int) {
    repeat(length) { index ->
        output[outputOffset + index] = this[inputOffset + index]
    }
}

private fun Int8Array.toByteArray(): ByteArray = ByteArray(length).also { copyInto(0, it, 0, length) }
