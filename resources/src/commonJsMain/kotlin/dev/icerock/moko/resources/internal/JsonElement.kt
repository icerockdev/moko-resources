/*
 * Copyright 2025 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.internal

// we not want to depends on kotlinx.serialization but need shared abstraction
internal sealed interface JsonElement {
    class Object(
        private val data: Map<String, JsonElement>
    ) : Map<String, JsonElement> by data, JsonElement

    class Array(
        private val data: List<JsonElement>
    ) : List<JsonElement> by data, JsonElement {
        val stringArray: List<String> = data.mapNotNull { it.stringPrimitive }
    }

    class StringPrimitive(val value: String) : JsonElement

    val stringPrimitive: String? get() = (this as? StringPrimitive)?.value
    val array: Array? get() = this as? Array
}
