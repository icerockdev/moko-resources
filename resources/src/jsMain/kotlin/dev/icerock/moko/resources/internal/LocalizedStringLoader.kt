/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.internal

import kotlinx.browser.window
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.w3c.fetch.Response
import kotlin.js.Json

class LocalizedStringLoader(
    private val supportedLocales: SupportedLocales,
    private val fallbackFileUri: String
) {
    private val cachedLocalizationFiles: MutableMap<String, Json> = mutableMapOf()
    private var cachedFallbackFile: Json? = null

//    suspend fun loadLocalizedString(
//        resName: String,
//        locale: SupportedLocale?,
//        fallbackFileUri: String
//    ): String {
//        TODO()
////        val localizationFile: Json = when (locale) {
////            null -> loadOrGetFallbackFile(fallbackFileUri)
////            in cachedLocalizationFiles -> {
////                cachedLocalizationFiles[locale]
////            }
////            else -> {
////                loadLocalizationFile(locale.fileUrl)
////            }
////        } ?: throw RuntimeException("Could not load localized string")
////
////        val localizedString = if (localizationFile[resName] == null) {
////            //fallback
////            val cachedFallbackFile = loadOrGetFallbackFile(fallbackFileUri)
////
////            cachedFallbackFile[resName] as? String
////        } else localizationFile[resName] as? String
////
////        return localizedString ?: throw RuntimeException("Could not load localized string")
//    }

    private suspend fun loadLocalizationFile(fileUri: String): Json {
        val response: Response = window.fetch(fileUri).await()
        if (!response.ok) throw IllegalStateException("response not ok for $fileUri - ${response.statusText} ${response.body}")

        @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
        val json: Json? = response.json().await() as Json?

        return json ?: throw RuntimeException("Could not read json at $fileUri")
    }

    suspend fun download() {
        coroutineScope {
            supportedLocales
                .map { async { downloadLocaleFile(it) } }
                .plus(async { downloadFallbackFile() })
                .awaitAll()
        }
    }

    private suspend fun downloadLocaleFile(locale: SupportedLocale) {
        if (cachedLocalizationFiles[locale.locale] != null) return
        cachedLocalizationFiles[locale.locale] = loadLocalizationFile(locale.fileUrl)
    }

    private suspend fun downloadFallbackFile() {
        if (cachedFallbackFile != null) return
        cachedFallbackFile = loadLocalizationFile(fallbackFileUri)
    }

    fun getString(key: String, locale: String?): String {
        val localeFile: Json = locale?.let { cachedLocalizationFiles[it] }
            ?: cachedFallbackFile
            ?: throw ResourcesNotLoaded(this)

        return localeFile[key]?.toString()
            ?: cachedFallbackFile?.get(key)?.toString()
            ?: throw IllegalArgumentException("$key not found")
    }
}
