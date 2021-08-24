/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import java.text.MessageFormat
import java.util.Locale

actual class StringResource(
    val resourcesClassLoader: ClassLoader,
    val bundleName: String,
    val key: String
) {
    fun localized(locale: Locale = Locale.getDefault()): String {
        val resourceBundle = resourcesClassLoader.getResourceBundle(bundleName, locale)
        return resourceBundle.getString(key)
    }

    fun localized(locale: Locale = Locale.getDefault(), vararg args: Any): String {
        val resourceBundle = resourcesClassLoader.getResourceBundle(bundleName, locale)
        val string = resourceBundle.getString(key)
        return MessageFormat.format(string, *args)
    }
}
