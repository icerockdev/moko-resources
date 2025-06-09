/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.provider

import dev.icerock.moko.resources.internal.JsObject
import dev.icerock.moko.resources.internal.SupportedLocale
import dev.icerock.moko.resources.internal.SupportedLocales
import kotlinx.browser.window
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.w3c.fetch.Response

fun JsStringProvider.Companion.loader(
    builder: RemoteJsStringLoaderBuilder.() -> Unit
): RemoteJsStringLoader = RemoteJsStringLoaderBuilder().apply(builder).build()

suspend fun JsStringProvider.Companion.load(
    builder: RemoteJsStringLoaderBuilder.() -> Unit
): JsStringProvider = loader(builder).getOrLoad()

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

fun interface RemoteJsStringLoader {
    class Impl(
        private val supportedLocales: SupportedLocales,
        private val fallbackFileUri: String
    ) : RemoteJsStringLoader {
        private val cachedLocalizationFiles: MutableMap<String, JsObject> = mutableMapOf()
        private var cachedFallbackFile: JsObject? = null

        private suspend fun loadLocalizationFile(fileUri: String): JsObject {
            val response: Response = window.fetch(fileUri).await()
            if (!response.ok) {
                error("response not ok for $fileUri - ${response.statusText} ${response.body}")
            }

            @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
            val json: JsObject? = response.json().await() as JsObject?

            return json ?: error("Could not read json at $fileUri")
        }

        override suspend fun getOrLoad(): JsStringProvider {
            coroutineScope {
                supportedLocales
                    .map { async { downloadLocaleFile(it) } }
                    .plus(async { downloadFallbackFile() })
                    .awaitAll()
            }
            return JsStringProvider { id, locale ->
                val localeFile: JsObject = locale?.let { cachedLocalizationFiles[locale] }
                    ?: cachedFallbackFile
                    ?: error("Invalid state after download")

                localeFile[id.toJsString()]?.toString()
                    ?: cachedFallbackFile?.get(id.toJsString())?.toString()
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
