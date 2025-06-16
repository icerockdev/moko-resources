/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import dev.icerock.moko.resources.internal.LocalizedText
import dev.icerock.moko.resources.internal.currentLocale
import dev.icerock.moko.resources.provider.JsStringProvider
import dev.icerock.moko.resources.provider.RemoteJsStringLoader

actual class PluralsResource(
    private val key: String,
    val loader: RemoteJsStringLoader
) {
    fun localized(provider: JsStringProvider, locale: String?, quantity: Int): String {
        val pluralString: String = provider.provideString(id = key, locale = locale)
        val pluralLocale: String = locale ?: currentLocale()
        return LocalizedText(locale = pluralLocale, text = pluralString).evaluate(quantity = quantity)
    }

    fun localized(provider: JsStringProvider, locale: String?, quantity: Int, vararg args: Any): String {
        val pluralString: String = provider.provideString(id = key, locale = locale)
        val pluralLocale: String = locale ?: currentLocale()
        return LocalizedText(locale = pluralLocale, text = pluralString).evaluate(quantity = quantity, args = args)
    }
}
