/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import dev.icerock.moko.resources.sprintf_js.sprintf
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.js.Json

actual class StringResource(
    private val key: String,
    private val supportedLocales: SupportedLocales,
    private val fallbackFileUri: String
) {

    companion object {
        private val cachedLocalizationFiles: MutableMap<SupportedLocale, Json> = mutableMapOf()

        private var cachedFallbackFile: Json? = null

        //Avoid spawning of multiple download threads. Remove if this isn't possible, but I am not sure.
        private val loadMutex = Mutex()

        private suspend fun loadLocalizedString(
            resName: String,
            locale: SupportedLocale?,
            fallbackFileUri: String
        ): String {
            loadMutex.withLock {
                val localizationFile: Json = when (locale) {
                    null -> loadOrGetFallbackFile(fallbackFileUri)
                    in cachedLocalizationFiles -> {
                        cachedLocalizationFiles[locale]
                    }
                    else -> {
                        loadLocalizationFile(locale.stringFileUri)
                            ?: //Localization file wasn't loaded, fallback
                            loadOrGetFallbackFile(fallbackFileUri)
                    }
                } ?: throw RuntimeException("Could not load localized string")

                val localizedString = if (localizationFile[resName] == null) {
                    //fallback
                    val cachedFallbackFile = loadOrGetFallbackFile(fallbackFileUri)

                    cachedFallbackFile?.get(resName) as? String
                } else localizationFile[resName] as? String

                return localizedString ?: throw RuntimeException("Could not load localized string")
            }
        }

        private suspend fun loadLocalizationFile(fileUri: String): Json? {
            @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
            return window.fetch(fileUri).await().json().await() as? Json
        }

        private suspend fun loadOrGetFallbackFile(fallbackFileUri: String): Json? {
            val current = cachedFallbackFile
            if (current != null) return current

            cachedFallbackFile = loadLocalizationFile(fallbackFileUri)
            return cachedFallbackFile
        }
    }

    suspend fun localized(locale: String? = null): String {
        return loadLocalizedString(key, findMatchingLocale(locale), fallbackFileUri)
    }

    suspend fun localized(locale: String? = null, vararg args: Any): String {
        val string = loadLocalizedString(key, findMatchingLocale(locale), fallbackFileUri)
        return sprintf(string, args)
    }

    private fun findMatchingLocale(locale: String?) =
        if (locale == null) getLanguageLocale(supportedLocales) else findMatchingLocale(
            supportedLocales,
            arrayOf(locale)
        )
}