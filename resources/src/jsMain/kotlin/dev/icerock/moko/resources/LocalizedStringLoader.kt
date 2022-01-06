/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlin.js.Json

class LocalizedStringLoader {
    private val cachedLocalizationFiles: MutableMap<SupportedLocale, Json> = mutableMapOf()

    private var cachedFallbackFile: Json? = null

    suspend fun loadLocalizedString(
        resName: String,
        locale: SupportedLocale?,
        fallbackFileUri: String
    ): String {
        val localizationFile: Json = when (locale) {
            null -> loadOrGetFallbackFile(fallbackFileUri)
            in cachedLocalizationFiles -> {
                cachedLocalizationFiles[locale]
            }
            else -> {
                loadLocalizationFile(locale.fileUri)
            }
        } ?: throw RuntimeException("Could not load localized string")

        val localizedString = if (localizationFile[resName] == null) {
            //fallback
            val cachedFallbackFile = loadOrGetFallbackFile(fallbackFileUri)

            cachedFallbackFile[resName] as? String
        } else localizationFile[resName] as? String

        return localizedString ?: throw RuntimeException("Could not load localized string")
    }

    private suspend fun loadLocalizationFile(fileUri: String): Json {
        return retryIO {
            @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
            window.fetch(fileUri).await().json().await() as Json?
                ?: throw RuntimeException("Could not load $fileUri")
        }
    }

    private suspend fun loadOrGetFallbackFile(fallbackFileUri: String): Json {
        val current = cachedFallbackFile
        if (current != null) return current

        val loadLocalizationFile = loadLocalizationFile(fallbackFileUri)
        cachedFallbackFile = loadLocalizationFile
        return loadLocalizationFile
    }
}