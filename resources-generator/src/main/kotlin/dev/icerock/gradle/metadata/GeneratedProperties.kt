package dev.icerock.gradle.metadata

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class GeneratedProperties(
    val modifier: GeneratedObjectModifier,
    val name: String,
    val data: JsonElement // Can contain data with dependencies from generator type
)
