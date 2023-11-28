package dev.icerock.gradle.metadata

import kotlinx.serialization.Serializable

@Serializable
data class GeneratedObject(
    val type: GeneratedObjectType,
    val name: String,
    val modifier: GeneratedObjectModifier,
    val properties: List<GeneratedProperties>,
) {
    val objectSpec: String
        get() = "${modifier.value} ${type.value} $name"
}
