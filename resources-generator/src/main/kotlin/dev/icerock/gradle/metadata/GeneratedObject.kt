package dev.icerock.gradle.metadata

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

data class GeneratedObject(
    val type: GeneratedObjectType,
    val name: String,
    val modifier: GeneratedObjectModifier,
    val properties: List<GeneratedProperties>,
) {
    val objectSpec: String
        get() = "${modifier.value} ${type.value} $name"

    val asJsonObject: JsonObject
        get() = buildJsonObject {
            put(key = KEY_NAME, value = name)
            put(key = KEY_TYPE, value = type.value)
            put(key = KEY_MODIFIER, value = modifier.value)
            put(
                key = KEY_PROPERTIES,
                element = buildJsonArray {
                    properties.forEach {
                        add(it.asJsonObject)
                    }
                }
            )
        }

    companion object {
        const val KEY_NAME = "name"
        const val KEY_TYPE = "type"
        const val KEY_MODIFIER = "modifier"
        const val KEY_PROPERTIES = "properties"
    }
}
