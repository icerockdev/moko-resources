/*
 * Copyright 2025 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.internal

import kotlinx.coroutines.flow.Flow

internal const val MEDIA_DARK_SCHEME = "(prefers-color-scheme: dark)"

expect abstract class Window

internal expect fun Window.isDarkMode(): Boolean
internal expect fun Window.getDarkModeFlow(): Flow<Boolean>

internal expect fun currentLocale(): String
internal expect fun getUserLanguages(): Array<out String>

internal expect suspend fun fetchJson(fileUri: String): JsonElement

internal expect suspend fun fetchText(fileUri: String): String

internal expect class LocalizedText(locale: String, text: String) {
    fun evaluate(quantity: Int, vararg args: Any): String
    fun evaluate(vararg args: Any): String
}
