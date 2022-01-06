/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import dev.icerock.moko.resources.sprintf_js.sprintf

actual class StringResource(
    private val key: String,
    private val supportedLocales: SupportedLocales,
    private val fallbackFileUri: String
) {

    companion object {
        private val stringResourceLoader = LocalizedStringLoader()
    }

    suspend fun localized(locale: String? = null): String {
        return stringResourceLoader.loadLocalizedString(
            key,
            findMatchingLocale(supportedLocales, locale),
            fallbackFileUri
        )
    }

    suspend fun localized(locale: String? = null, vararg args: Any): String {
        val string =
            stringResourceLoader.loadLocalizedString(key, findMatchingLocale(supportedLocales, locale), fallbackFileUri)
        return sprintf(string, args)
    }
}