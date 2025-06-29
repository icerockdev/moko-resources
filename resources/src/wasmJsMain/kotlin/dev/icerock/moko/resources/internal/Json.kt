/*
 * Copyright 2025 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.internal

@JsName("Object")
external class JsObject : JsAny {
    operator fun get(key: JsString): JsAny?
    operator fun set(key: JsString, value: JsAny?)
}

internal fun json(vararg entries: Pair<String, Any>) =
    entries.fold(JsObject()) { result, (key, value) ->
        result.apply {
            set(key.toJsString(), value.toString().toJsString())
        }
    }

internal fun JsAny.toJsonElement(): JsonElement {
    return when (this) {
        is JsArray<*> -> {
            val array = this.toList()
                .map { it?.toJsonElement() ?: JsonElement.StringPrimitive("null") }
            JsonElement.Array(array)
        }
        is JsObject -> {
            val objectProperties = jsObjectKeys(this).toList().associate { propertyName ->
                val strKey = propertyName.toString()
                this[propertyName]?.toJsonElement()
                    ?.let { strKey to it }
                    ?: (strKey to JsonElement.StringPrimitive("null"))
            }
            JsonElement.Object(objectProperties)
        }
        else -> JsonElement.StringPrimitive(this.toString())
    }
}

private fun jsObjectKeys(jsObject: JsObject): JsArray<JsString> = js(
    "Object.keys(jsObject)"
)

private fun <T : JsAny?> JsArray<T>.toList(): List<T> {
    @Suppress("UNCHECKED_CAST")
    return List(length) { this[it] as T }
}
