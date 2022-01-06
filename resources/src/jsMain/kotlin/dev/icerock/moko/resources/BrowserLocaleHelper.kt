/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

import dev.icerock.moko.resources.bcp_47.parse
import kotlinx.browser.window
import kotlin.js.Json

@Suppress("UNCHECKED_CAST")
class ParsedLocale(parseResult: Json) {
    val primaryLanguageTag: String = parseResult["language"] as? String
        ?: throw IllegalArgumentException("Primary language tags must not be empty")

    val extendedLanguageSubtag: List<String> =
        (parseResult["extendedLanguageSubtags"] as? Array<String>)?.toList().orEmpty()

    val script: String = (parseResult["script"] as? String).orEmpty()

    val region: String = (parseResult["region"] as? String).orEmpty()

    val variants: List<String> = (parseResult["variants"] as? Array<String>)?.toList().orEmpty()

    val extensions: List<String> = (parseResult["extensions"] as? Array<String>)?.toList().orEmpty()

    val privateuse: List<String> = (parseResult["privateuse"] as? Array<String>)?.toList().orEmpty()

    //For now we skip irregular and regular.
}

private var cachedLocale: CachedLocale? = null

/**
 * @return the locale that can be used to localize strings or null if no locale was found that matches this user's preferences
 */
fun getLanguageLocale(supportedLocales: SupportedLocales): SupportedLocale? {
    val currentCache = cachedLocale
    val userLanguages = window.navigator.languages
    return if (currentCache != null && currentCache.usedLanguages.contentEquals(userLanguages)) {
        currentCache.locale
    } else {
        val foundLocale = findMatchingLocale(supportedLocales)
        cachedLocale = CachedLocale(userLanguages, foundLocale)
        return foundLocale
    }
}

fun findMatchingLocale(
    supportedLocales: SupportedLocales,
    userLanguages: Array<out String> = window.navigator.languages
): SupportedLocale? {
    return userLanguages
        .asSequence()
        .map { localeString ->
            val parsedLocale = parseBcpLocale(localeString)

            val potentialLanguages =
                supportedLocales.getLocalesForLanguage(parsedLocale.primaryLanguageTag)

            val mostMatchingLocale = potentialLanguages
                .map { potentialLanguage ->
                    potentialLanguage to calculateMatchingScore(
                        parsedLocale,
                        potentialLanguage
                    )
                }
                .filter { (_, score) -> score != -1 }
                .maxByOrNull { (_, score) -> score }
                ?.first

            mostMatchingLocale
        }
        .firstOrNull()
}

/**
 * Calculates a score that reflects how much of a match the candidate is.
 * A score of -1 means that the locale does not match.
 *
 * If the desired locale is de_DE and the candidate is de_DE, there is a perfect match and it will have the highest score.
 * For de_DE and candidate de the score will be below de_DE but not 0
 * For en_UK and candidate en_US the score will be -1 as en_UK and en_US do not fit together
 */
private fun calculateMatchingScore(desiredLocale: ParsedLocale, candidate: SupportedLocale): Int {
    var score = 0

    //Checks if all elements of the candidate list are also in the desired list. Then adds 1 score for every element that matches.
    val listComparison: (desiredList: List<String>, candidateList: List<String>) -> Boolean =
        { desiredList, candidateList ->
            if (desiredList.containsAll(candidateList)) {
                score += desiredList.count { it in candidateList }
                true
            } else false
        }

    if (!listComparison(desiredLocale.extendedLanguageSubtag, candidate.parsedLocale.extendedLanguageSubtag)) return -1

    if (desiredLocale.script == candidate.parsedLocale.script) {
        score++
    } else if (desiredLocale.script.isNotEmpty() && candidate.parsedLocale.script.isNotEmpty()) return -1

    if (desiredLocale.region == candidate.parsedLocale.region) {
        score++
    } else if (desiredLocale.region.isNotEmpty() && candidate.parsedLocale.script.isNotEmpty()) return -1

    if (!listComparison(desiredLocale.variants, candidate.parsedLocale.variants)) return -1
    if (!listComparison(desiredLocale.extensions, candidate.parsedLocale.extensions)) return -1
    if (!listComparison(desiredLocale.privateuse, candidate.parsedLocale.privateuse)) return -1

    return score
}

private class CachedLocale(val usedLanguages: Array<out String>, val locale: SupportedLocale?)

fun parseBcpLocale(tag: String): ParsedLocale = ParsedLocale(parse(tag))