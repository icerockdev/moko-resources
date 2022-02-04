/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.internal

class ResourcesNotLoaded(
    private val loader: LocalizedStringLoader
) : RuntimeException("Resources not loaded at now. Please call loader.download()") {
    suspend fun download() {
        loader.download()
    }
}
