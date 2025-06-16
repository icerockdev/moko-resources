/*
 * Copyright 2025 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import dev.icerock.moko.resources.internal.fetchText
import dev.icerock.moko.resources.internal.retryIO

actual class FileResource(val fileUrl: String) {
    suspend fun getText(): String {
        return retryIO {
            fetchText(fileUrl)
        }
    }
}
