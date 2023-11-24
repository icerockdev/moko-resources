package dev.icerock.gradle.metadata

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

data class GeneratedVariables(
    val name: String,
    val modifier: GeneratedObjectModifier,
) {
    val asJsonObject: JsonObject
        get() = buildJsonObject {
            put("name", name)
            put("modifier", modifier.value)
        }
}
