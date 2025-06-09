/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import dev.icerock.moko.resources.internal.JsObject
import dev.icerock.moko.resources.internal.currentLocale
import dev.icerock.moko.resources.internal.messageFormat.CompiledPlural
import dev.icerock.moko.resources.internal.messageFormat.MessageFormat
import dev.icerock.moko.resources.provider.JsStringProvider
import dev.icerock.moko.resources.provider.RemoteJsStringLoader

actual class PluralsResource(
    private val key: String,
    val loader: RemoteJsStringLoader
) {
    fun localized(provider: JsStringProvider, locale: String?, quantity: Int): String {
        val pluralString: String = provider.provideString(id = key, locale = locale)
        val pluralLocale: String = locale ?: currentLocale()
        val jsArrayPluralLocale = JsArray<JsString>()
        jsArrayPluralLocale[0] = pluralLocale.toJsString()

        val compiledPlural: (JsObject) -> String = MessageFormat(jsArrayPluralLocale)
            .compile(pluralString)

        return CompiledPlural(compiledPlural).evaluate(quantity)
    }

    fun localized(provider: JsStringProvider, locale: String?, quantity: Int, vararg args: Any): String {
        val pluralString: String = provider.provideString(id = key, locale = locale)
        val pluralLocale: String = locale ?: currentLocale()
        val jsArrayPluralLocale = JsArray<JsString>()
        jsArrayPluralLocale[0] = pluralLocale.toJsString()

        val compiledPlural = CompiledPlural(
            MessageFormat(jsArrayPluralLocale).compile(pluralString)
        )

        return compiledPlural.evaluate(quantity, *args)
    }
}
