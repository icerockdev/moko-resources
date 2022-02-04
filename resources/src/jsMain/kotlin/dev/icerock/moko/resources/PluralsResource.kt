/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import dev.icerock.moko.resources.internal.SupportedLocales

actual class PluralsResource(
    private val key: String,
    private val supportedLocales: SupportedLocales,
    private val fallbackFileUri: String
) {
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