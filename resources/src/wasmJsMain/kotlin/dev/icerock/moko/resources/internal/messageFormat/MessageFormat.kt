/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.internal.messageFormat

import dev.icerock.moko.resources.internal.JsObject
import dev.icerock.moko.resources.internal.json

value class CompiledVariableString(private val function: (JsObject) -> String) {
    fun evaluate(vararg args: Any): String {
        val keyValues = args.mapIndexed { index: Int, any: Any -> "$index" to any }

        @Suppress("SpreadOperator")
        val json = json(*keyValues.toTypedArray())
        return function(json)
    }
}

value class CompiledPlural(private val function: (JsObject) -> String) {
    fun evaluate(quantity: Int, vararg args: Any): String {
        val keyValues = arrayOf("PLURAL" to quantity as Any) + args
            .mapIndexed { index: Int, any: Any -> "$index" to any }

        @Suppress("SpreadOperator")
        val json = json(*keyValues)
        return function(json)
    }
}

@JsModule("@messageformat/core")
external class MessageFormat(@Suppress("UnusedPrivateMember") locales: JsArray<JsString>) {
    fun compile(format: String): (JsObject) -> String
}
