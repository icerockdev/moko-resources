/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */


package dev.icerock.moko.resources.internal.message_format

import kotlin.js.Json
import kotlin.js.json

value class CompiledVariableString(private val function: (Json) -> String) {
    fun evaluate(vararg args: Any): String {
        val keyValues = args.mapIndexed { index: Int, any: Any -> "$index" to any }

        val json = json(*keyValues.toTypedArray())
        return function(json)
    }
}

value class CompiledPlural(private val function: (Json) -> String) {
    fun evaluate(quantity: Int, vararg args: Any): String {
        val keyValues = arrayOf("PLURAL" to quantity) +
                args.mapIndexed { index: Int, any: Any -> "$index" to any }

        val json = json(*keyValues)
        return function(json)
    }
}

@JsModule("@messageformat/core")
external class MessageFormat(locales: Array<String>) {
    fun compile(format: String): (Json) -> String
}