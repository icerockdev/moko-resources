package dev.icerock.gradle.metadata

import kotlinx.serialization.Serializable

@Serializable
data class GeneratedObject(
    val generatorType: GeneratorType,
    val modifier: GeneratedObjectModifier,
    val type: GeneratedObjectType,
    val name: String,
    val interfaces: List<String> = emptyList(),
    val properties: List<GeneratedProperties> = emptyList(),
    val objects: List<GeneratedObject> = emptyList()
)
