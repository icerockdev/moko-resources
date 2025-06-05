/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import dev.icerock.moko.resources.internal.retryIO
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.w3c.fetch.Response

actual class AssetResource(
    // path after webpack serving
    actual val originalPath: String,
    // path identifier
    val rawPath: String
) {
    suspend fun getText(): String {
        return retryIO {
            window.fetch(originalPath).await<Response>().text().await()
        }
    }
}
