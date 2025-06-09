/*
 * Copyright 2025 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.provider

fun interface JsStringProvider {
    fun provideString(id: String, locale: String?): String

    operator fun plus(other: JsStringProvider) = JsStringProvider { id, locale ->
        runCatching {
            provideString(id, locale)
        }.recover {
            other.provideString(id, locale)
        }.getOrThrow()
    }

    companion object
}
