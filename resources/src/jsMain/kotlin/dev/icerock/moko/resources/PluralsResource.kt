/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import dev.icerock.moko.resources.internal.LocalizedStringLoader
import dev.icerock.moko.resources.internal.currentLocale
import dev.icerock.moko.resources.internal.message_format.CompiledPlural
import dev.icerock.moko.resources.internal.message_format.MessageFormat
import kotlin.js.Json

actual class PluralsResource(
    private val key: String,
    private val loader: LocalizedStringLoader
) {
    fun localized(locale: String?, quantity: Int): String {
        val pluralString: String = loader.getString(key = key, locale = locale)
        val pluralLocale: String = locale
            ?: currentLocale()
            ?: throw IllegalStateException("can't get locale")

        val compiledPlural: (Json) -> String = MessageFormat(arrayOf(pluralLocale))
            .compile(pluralString)

        return CompiledPlural(compiledPlural).evaluate(quantity)
    }

//    private var cachedValue: CachedValue? = null
//
//    companion object {
//        private val pluralsResourceLoader = LocalizedStringLoader()
//    }
//
//    suspend fun localized(locale: String? = null, quantity: Int, vararg args: Any): String {
//        val currentCache = cachedValue
//        if (currentCache != null && currentCache.locale == locale) {
//            return currentCache.plural.evaluate(quantity, args)
//        }
//
//        //Cache miss
//        val usedLocale = findMatchingLocale(supportedLocales, locale)
//        val localizedString = pluralsResourceLoader.loadLocalizedString(
//            key,
//            usedLocale,
//            fallbackFileUri
//        )
//
//        val compiledPlural = CompiledPlural(
//            MessageFormat(arrayOf(usedLocale?.locale ?: "en"))
//                .compile(localizedString)
//        )
//
//        cachedValue = CachedValue(locale, compiledPlural)
//
//        return compiledPlural.evaluate(quantity, args)
//    }
//
//    private class CachedValue(val locale: String?, val plural: CompiledPlural)
}