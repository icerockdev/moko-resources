/*
 * Copyright 2025 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.internal

import dev.icerock.moko.resources.internal.messageFormat.CompiledPlural
import dev.icerock.moko.resources.internal.messageFormat.CompiledVariableString
import dev.icerock.moko.resources.internal.messageFormat.MessageFormat
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.w3c.dom.MediaQueryList
import org.w3c.dom.Window
import org.w3c.dom.events.Event
import org.w3c.fetch.Response

internal actual typealias Window = org.w3c.dom.Window

internal actual fun currentLocale(): String {
    return window.navigator.language
}

internal actual fun getUserLanguages(): Array<out String> {
    return Array(window.navigator.languages.length) { window.navigator.languages[it].toString() }
}

internal actual fun Window.isDarkMode(): Boolean {
    return matchMedia(MEDIA_DARK_SCHEME).matches
}

internal actual fun Window.getDarkModeFlow(): Flow<Boolean> {
    return channelFlow {
        val mediaList: MediaQueryList = window.matchMedia(MEDIA_DARK_SCHEME)
        val listener: (Event) -> Unit = { _: Event ->
            trySend(isDarkMode())
        }
        mediaList.addListener(listener)

        invokeOnClose {
            mediaList.removeListener(listener)
        }
    }
}

internal actual suspend fun fetchJson(fileUri: String): JsonElement {
    val response: Response = window.fetch(fileUri).await()
    if (!response.ok) {
        error("response not ok for $fileUri - ${response.statusText} ${response.body}")
    }

    @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
    val json: JsObject? = response.json().await() as JsObject?

    return json?.toJsonElement() ?: error("Could not read json at $fileUri")
}

actual suspend fun fetchText(fileUri: String): String {
    return window.fetch(fileUri).await<Response>().text().await<JsString>().toString()
}

internal actual class LocalizedText actual constructor(locale: String, text: String) {
    private val function: (JsObject) -> String = MessageFormat(
        locales = JsArray<JsString>().apply { set(0, locale.toJsString()) }
    ).compile(text)

    actual fun evaluate(quantity: Int, vararg args: Any): String {
        return CompiledPlural(function).evaluate(quantity, *args)
    }

    actual fun evaluate(vararg args: Any): String {
        return CompiledVariableString(function).evaluate(*args)
    }
}
