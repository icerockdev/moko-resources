/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import dev.icerock.moko.resources.internal.currentLocale
import dev.icerock.moko.resources.internal.messageFormat.CompiledVariableString
import dev.icerock.moko.resources.internal.messageFormat.MessageFormat
import dev.icerock.moko.resources.provider.JsStringProvider
import dev.icerock.moko.resources.provider.RemoteJsStringLoader

actual class StringResource(
    private val key: String,
    val loader: RemoteJsStringLoader
) {
    fun localized(provider: JsStringProvider, locale: String?): String {
        return provider.provideString(id = key, locale = locale)
    }

    fun localized(provider: JsStringProvider, locale: String?, vararg args: Any): String {
        val rawString: String = provider.provideString(id = key, locale = locale)
        val compiled = CompiledVariableString(
            MessageFormat(arrayOf(locale ?: currentLocale())).compile(rawString)
        )
        return compiled.evaluate(*args)
    }
}
