/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

class SupportedLocales(private val locales: List<SupportedLocale>) {

    private val languageMap: Map<String, List<SupportedLocale>> =
        locales
            .groupBy { it.parsedLocale.primaryLanguageTag }

    fun getLocalesForLanguage(primaryLanguageSubtag: String): List<SupportedLocale> =
        languageMap[primaryLanguageSubtag] ?: emptyList()
}