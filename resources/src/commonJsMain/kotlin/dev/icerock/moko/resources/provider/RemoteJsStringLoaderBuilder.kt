/*
 * Copyright 2025 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.provider

import dev.icerock.moko.resources.internal.SupportedLocale
import dev.icerock.moko.resources.internal.SupportedLocales

class RemoteJsStringLoaderBuilder {
    private val supportedLocales: MutableList<SupportedLocale> = mutableListOf()
    lateinit var fallbackFileUri: String

    fun locale(name: String, fileUri: String) {
        supportedLocales += SupportedLocale(locale = name, fileUrl = fileUri)
    }

    fun build(): RemoteJsStringLoader {
        require(::fallbackFileUri.isInitialized) { "Fallback file uri was not initialized" }

        return RemoteJsStringLoader.Impl(SupportedLocales(supportedLocales), fallbackFileUri)
    }
}
