/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.js

private val messageFormatRegex = "%(.)(?:\\\$(.))?".toRegex()

fun String.convertToMessageFormat(): String {
    val allMatches = messageFormatRegex
        .findAll(this)

    if (allMatches.count() == 0) return this

    var counter = 0
    var result = this

    // First go through the positioned args
    allMatches
        .filter { matchResult -> matchResult.groupValues[2].isNotEmpty() }
        .map { matchResult -> matchResult.groupValues[0] to matchResult.groupValues[1] }
        .distinctBy { it.second }
        .forEach { (wholeMatch, index) ->
            val intIndex = index.toIntOrNull() ?: error("Localized string $this uses positioned " +
                    "argument $wholeMatch but $index is not an integer.")

            result = result.replace(wholeMatch, "{${intIndex - 1}}")
            counter = intIndex
        }

    // Now remove the not positioned args
    while (messageFormatRegex.containsMatchIn(result)) {
        result = messageFormatRegex.replaceFirst(result, "{$counter}")
        counter++
    }

    return result
}
