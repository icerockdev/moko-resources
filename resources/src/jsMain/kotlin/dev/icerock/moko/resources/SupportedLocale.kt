/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources

data class SupportedLocale(val locale: String, val stringFileUri: String) {
    val parsedLocale: ParsedLocale = parseBcpLocale(locale)
}