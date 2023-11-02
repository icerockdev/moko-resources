/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.utils

import java.util.Locale

/**
 * Replace all new lines including space characters before and after.
 * This is required to remove the IDE indentation.
 */
internal fun String.removeLineWraps(): String {
    return replace(Regex("\\s*\n\\s*"), " ")
}

internal val String.withoutScale
    get() = substringBefore("@")

internal fun String.capitalize(): String {
    return replaceFirstChar { char ->
        if (char.isLowerCase()) char.titlecase(Locale.ROOT) else char.toString()
    }
}

internal fun String.decapitalize(): String {
    return replaceFirstChar { it.lowercase(Locale.ROOT) }
}

internal fun String.remove(char: Char): String {
    return this.remove(char.toString())
}

internal fun String.remove(char: String): String {
    return this.replace(char, "")
}

internal val String.flatName: String
    get() = this.remove('.')
