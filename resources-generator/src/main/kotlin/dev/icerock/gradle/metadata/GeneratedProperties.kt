package dev.icerock.gradle.metadata

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

data class GeneratedProperties(
    val name: String,
    val modifier: GeneratedObjectModifier,
) {
    val asJsonObject: JsonObject
        get() = buildJsonObject {
            put(key = KEY_NAME, value = name)
            put(key = KEY_MODIFIER, value = modifier.value)
        }

    companion object {
        const val KEY_NAME = "name"
        const val KEY_MODIFIER = "modifier"
    }
}
