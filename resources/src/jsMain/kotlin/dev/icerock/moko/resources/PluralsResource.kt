/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import dev.icerock.moko.resources.internal.currentLocale
import dev.icerock.moko.resources.internal.message_format.CompiledPlural
import dev.icerock.moko.resources.internal.message_format.MessageFormat
import dev.icerock.moko.resources.provider.JsStringProvider
import dev.icerock.moko.resources.provider.RemoteJsStringLoader
import kotlin.js.Json

actual class PluralsResource(
    private val key: String,
    val loader: RemoteJsStringLoader
) {
    fun localized(provider: JsStringProvider, locale: String?, quantity: Int): String {
        val pluralString: String = provider.provideString(id = key, locale = locale)
        val pluralLocale: String = locale ?: currentLocale()

        val compiledPlural: (Json) -> String = MessageFormat(arrayOf(pluralLocale))
            .compile(pluralString)

        return CompiledPlural(compiledPlural).evaluate(quantity)
    }

    fun localized(provider: JsStringProvider, locale: String?, quantity: Int, vararg args: Any): String {
        val pluralString: String = provider.provideString(id = key, locale = locale)
        val pluralLocale: String = locale ?: currentLocale()

        val compiledPlural = CompiledPlural(
            MessageFormat(arrayOf(pluralLocale)).compile(pluralString)
        )

        return compiledPlural.evaluate(quantity, *args)
    }
}
