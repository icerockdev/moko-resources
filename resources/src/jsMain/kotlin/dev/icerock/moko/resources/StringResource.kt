/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import dev.icerock.moko.resources.message_format.CompiledVariableString
import dev.icerock.moko.resources.message_format.MessageFormat

actual class StringResource(
    private val key: String,
    private val supportedLocales: SupportedLocales,
    private val fallbackFileUrl: String
) {

    private var cachedValue: CachedValue? = null

    companion object {
        private val stringResourceLoader = LocalizedStringLoader()
    }

    suspend fun localized(locale: String? = null, vararg args: Any): String {
        val currentCache = cachedValue
        if (currentCache != null && currentCache.locale == locale) {
            return currentCache.compiledVariableString.evaluate(args)
        }

        val usedLocale = findMatchingLocale(supportedLocales, locale)

        val localizedString = stringResourceLoader.loadLocalizedString(key, usedLocale, fallbackFileUrl)

        val compiledVariableString = CompiledVariableString(
            MessageFormat(arrayOf(usedLocale?.locale ?: "en"))
                .compile(localizedString)
        )

        cachedValue = CachedValue(locale, compiledVariableString)

        return compiledVariableString.evaluate(args)
    }

    private class CachedValue(val locale: String?, val compiledVariableString: CompiledVariableString)
}