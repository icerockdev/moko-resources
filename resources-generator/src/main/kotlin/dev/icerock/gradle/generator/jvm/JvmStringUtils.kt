/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.jvm

import org.apache.commons.text.StringEscapeUtils

internal fun convertXmlStringToJvmLocalization(input: String): String {
    val xmlDecoded = StringEscapeUtils.unescapeXml(input)
    return xmlDecoded.replace("\n", "\\n")
        .replace("\"", "\\\"")
}
