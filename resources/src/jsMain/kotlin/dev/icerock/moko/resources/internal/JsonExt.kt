/*
 * Copyright 2025 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.internal

internal fun Any.toJsonElement(): JsonElement {
    return when {
        this is Array<*> -> {
            val array = this.map { it?.toJsonElement() ?: JsonElement.StringPrimitive("null") }
            JsonElement.Array(
                array
            )
        }
        jsTypeOf(this) == "object" -> {
            val objectProperties = jsObjectToKotlinMap(this).mapValues { (_, v) ->
                v?.toJsonElement() ?: JsonElement.StringPrimitive("null")
            }
            JsonElement.Object(objectProperties)
        }
        else -> JsonElement.StringPrimitive(this.toString())
    }
}

private fun jsObjectToKotlinMap(jsObject: dynamic): Map<String, Any?> {
    val propertiesKeys = js("Object.keys(jsObject)") as Array<String>
    return propertiesKeys.associate { it to jsObject[it] }
}
