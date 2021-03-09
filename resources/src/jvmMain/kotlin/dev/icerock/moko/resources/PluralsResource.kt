/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import java.text.ChoiceFormat
import java.text.Format
import java.text.MessageFormat
import java.util.Locale

actual class PluralsResource(
    val resourcesClassLoader: ClassLoader,
    val bundleName: String,
    val key: String,
    val numberFormat: List<Pair<Double, String>>
) {
    fun localized(locale: Locale = Locale.getDefault(), quantity: Int): String {
        val resourceBundle = resourcesClassLoader.getResourceBundle(bundleName, locale)

        val limits = numberFormat.map { it.first }
        val strings = numberFormat.map { resourceBundle.getString(it.second) }
        val choiceFormat = ChoiceFormat(limits.toDoubleArray(), strings.toTypedArray())

        val message = resourceBundle.getString(key)
        val messageFormat = MessageFormat(message, locale)
        messageFormat.formats = arrayOf<Format?>(choiceFormat)

        val messageArguments: Array<out Any> = arrayOf(quantity)
        return messageFormat.format(messageArguments)
    }

    fun localized(locale: Locale = Locale.getDefault(), quantity: Int, vararg args: Any): String {
        val resourceBundle = resourcesClassLoader.getResourceBundle(bundleName, locale)

        val limits = numberFormat.map { it.first }
        val strings = numberFormat.map { resourceBundle.getString(it.second) }
        val choiceFormat = ChoiceFormat(limits.toDoubleArray(), strings.toTypedArray())

        val message = resourceBundle.getString(key)
        val messageFormat = MessageFormat(message, locale)
        messageFormat.formats =
            arrayOf<Format?>(choiceFormat) + args.map { null }.toTypedArray<Format?>()

        val messageArguments: Array<out Any> = arrayOf(quantity, *args)
        return messageFormat.format(messageArguments)
    }
}
