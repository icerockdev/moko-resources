package dev.icerock.gradle.metadata

import kotlinx.serialization.Serializable

@Serializable
data class GeneratedProperties(
    val modifier: GeneratedObjectModifier,
    val name: String,
    val data: String // Can contain data with dependencies from generator type
)
