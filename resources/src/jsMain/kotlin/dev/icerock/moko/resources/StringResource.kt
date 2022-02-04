/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import dev.icerock.moko.resources.internal.LocalizedStringLoader
import dev.icerock.moko.resources.internal.currentLocale
import dev.icerock.moko.resources.internal.message_format.CompiledVariableString
import dev.icerock.moko.resources.internal.message_format.MessageFormat

actual class StringResource(
    private val key: String,
    private val loader: LocalizedStringLoader
) {
    fun localized(locale: String?): String {
        return loader.getString(key = key, locale = locale)
    }

    fun localized(locale: String?, vararg args: Any): String {
        val rawString: String = loader.getString(key = key, locale = locale)
        val compiled = CompiledVariableString(
            MessageFormat(arrayOf(locale ?: currentLocale())).compile(rawString)
        )
        return compiled.evaluate(*args)
    }
}
