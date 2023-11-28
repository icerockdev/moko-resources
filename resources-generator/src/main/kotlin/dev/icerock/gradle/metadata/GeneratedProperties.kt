package dev.icerock.gradle.metadata

import kotlinx.serialization.Serializable

@Serializable
data class GeneratedProperties(
    val name: String,
    val modifier: GeneratedObjectModifier,
)
