/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.platform.js

import dev.icerock.gradle.utils.remove

private val messageFormatRegex =
    "%(\\d+\\\$)?(\\d*\\.?\\d+)?[bBhHsScCdoxXeEfgGaAtT]+".toRegex()

internal fun String.convertToMessageFormat(): String {
    val allMatches = messageFormatRegex.findAll(this)

    if (allMatches.count() == 0) return this

    var counter = 0
    var result = this

    // First go through the positioned args
    allMatches
        .filter { matchResult: MatchResult ->
            matchResult.groupValues[1].isNotEmpty()
        }
        .map { matchResult ->
            matchResult.groupValues[0] to matchResult.groupValues[1].remove('$')
        }
        .distinctBy { it.second }
        .forEach { (wholeMatch, index) ->
            val intIndex = index.toIntOrNull()
                ?: error(
                    "Localized string:\n{$this}\nuses positioned argument" +
                        " $wholeMatch but $index is not an integer."
                )

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
