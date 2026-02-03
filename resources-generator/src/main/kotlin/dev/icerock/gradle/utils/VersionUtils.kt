/*
 * Copyright 2025 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.utils

/**
 * Checks if the [currentVersion] is greater than or equal to the [minVersion].
 *
 * Both versions are expected to be in the format `major.minor.patch` (e.g., "9.0.0").
 * Pre-release suffixes such as "-beta", "-alpha", "-rc" are ignored.
 *
 * The comparison rules are:
 * 1. Compare major versions. If current major > min major, return true; if less, return false.
 * 2. If majors are equal, compare minor versions similarly.
 * 3. If both major and minor are equal, compare patch versions.
 *
 * Examples:
 * ```
 * hasMinimalVersion("8.1.0", "8.11.1") // true
 * hasMinimalVersion("9.0.0", "8.11.1") // false
 * hasMinimalVersion("9.0.0", "9.0.0-beta3") // true
 * ```
 *
 * @param minVersion the minimal required version (inclusive)
 * @param currentVersion the version to check against the minimal version
 * @return true if [currentVersion] is greater than or equal to [minVersion], false otherwise
 */
internal fun hasMinimalVersion(minVersion: String, currentVersion: String): Boolean {
    val (minMaj, minMin, minPatch) = parseVersion(minVersion)
    val (curMaj, curMin, curPatch) = parseVersion(currentVersion)

    return when {
        curMaj > minMaj -> true
        curMaj < minMaj -> false

        curMin > minMin -> true
        curMin < minMin -> false

        else -> curPatch >= minPatch
    }
}

/**
 * Parses a version string into a Triple of integers representing major, minor, and patch numbers.
 *
 * Pre-release suffixes such as "-beta", "-alpha", "-rc" are ignored.
 * Missing minor or patch numbers default to 0.
 *
 * Examples:
 * ```
 * parseVersion("9.0.0-beta3") // Triple(9,0,0)
 * parseVersion("8.11")        // Triple(8,11,0)
 * parseVersion("7")           // Triple(7,0,0)
 * ```
 *
 * @param raw the version string to parse
 * @return a [Triple] containing major, minor, and patch numbers
 */
private fun parseVersion(raw: String): Triple<Int, Int, Int> {
    val core: String = raw.substringBefore('-') // remove alpha/beta/rc/etc
    val parts: List<String> = core.split('.')

    val major: Int = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val minor: Int = parts.getOrNull(1)?.toIntOrNull() ?: 0
    val patch: Int = parts.getOrNull(2)?.toIntOrNull() ?: 0

    return Triple(major, minor, patch)
}
