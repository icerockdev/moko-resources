/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import com.ibm.icu.text.PluralRules
import com.ibm.icu.util.ULocale
import java.util.*

actual class PluralsResource(
    val resourcesClassLoader: ClassLoader,
    val bundleName: String,
    val key: String
) {

    private fun getPluralMessage(locale: Locale, quantity: Int): String {
        val resourceBundle = resourcesClassLoader.getResourceBundle(bundleName, locale)

        val pluralRules = PluralRules.forLocale(ULocale.forLocale(locale))
        val selectedVariant = pluralRules.select(quantity.toDouble())
        val keyWithQuantity = "$key.$selectedVariant"

        return if (resourceBundle.containsKey(keyWithQuantity)) {
            resourceBundle.getString(keyWithQuantity)
        } else {
            resourceBundle.getString("$key.${PluralRules.KEYWORD_OTHER}")
        }
    }

    fun localized(locale: Locale = Locale.getDefault(), quantity: Int): String =
        getPluralMessage(locale, quantity)

    fun localized(locale: Locale = Locale.getDefault(), quantity: Int, vararg args: Any?): String =
        String.format(locale = locale, getPluralMessage(locale, quantity), *args)
}
