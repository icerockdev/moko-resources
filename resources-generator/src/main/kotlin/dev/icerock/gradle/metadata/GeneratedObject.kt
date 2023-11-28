package dev.icerock.gradle.metadata

import kotlinx.serialization.Serializable

@Serializable
data class GeneratedObject(
    val modifier: GeneratedObjectModifier,
    val type: GeneratedObjectType,
    val name: String,
    val properties: List<GeneratedProperties>,
) {
    val objectSpec: String
        get() = "${modifier.value} ${type.value} $name"
}
