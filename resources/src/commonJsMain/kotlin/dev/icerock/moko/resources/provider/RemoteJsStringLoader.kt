/*
 * Copyright 2025 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.provider

import dev.icerock.moko.resources.internal.JsonElement
import dev.icerock.moko.resources.internal.SupportedLocale
import dev.icerock.moko.resources.internal.SupportedLocales
import dev.icerock.moko.resources.internal.fetchJson
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

fun interface RemoteJsStringLoader {
    class Impl(
        private val supportedLocales: SupportedLocales,
        private val fallbackFileUri: String
    ) : RemoteJsStringLoader {
        private val cachedLocalizationFiles: MutableMap<String, JsonElement.Object> = mutableMapOf()
        private var cachedFallbackFile: JsonElement.Object? = null

        private suspend fun loadLocalizationFile(fileUri: String): JsonElement.Object {
            val jsonFile: JsonElement = fetchJson(fileUri)

            return jsonFile as? JsonElement.Object ?: error("Could not read json object at $fileUri")
        }

        override suspend fun getOrLoad(): JsStringProvider {
            coroutineScope {
                supportedLocales
                    .map { async { downloadLocaleFile(it) } }
                    .plus(async { downloadFallbackFile() })
                    .awaitAll()
            }
            return JsStringProvider { id, locale ->
                val localeFile: JsonElement.Object = locale?.let { cachedLocalizationFiles[locale] }
                    ?: cachedFallbackFile
                    ?: error("Invalid state after download")

                localeFile[id]?.stringPrimitive
                    ?: cachedFallbackFile?.get(id)?.stringPrimitive
                    ?: error("$id string resource not found")
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
    }

    class Composition(val loaders: List<RemoteJsStringLoader>) : RemoteJsStringLoader {
        override suspend fun getOrLoad(): JsStringProvider {
            val providers = coroutineScope {
                loaders.map {
                    async {
                        it.getOrLoad()
                    }
                }.awaitAll()
            }
            return providers.reduce { acc, jsStringProvider -> acc + jsStringProvider }
        }
    }

    suspend fun getOrLoad(): JsStringProvider

    operator fun plus(other: RemoteJsStringLoader) = Composition(listOf(this, other))
}
