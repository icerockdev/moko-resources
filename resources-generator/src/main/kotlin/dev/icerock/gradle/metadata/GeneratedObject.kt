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
) {
    val isExpect: Boolean
        get() = modifier == GeneratedObjectModifier.Expect

    val isActual: Boolean
        get() = modifier == GeneratedObjectModifier.Actual

    val isObject: Boolean
        get() = type == GeneratedObjectType.Object

    val isInterface: Boolean
        get() = type == GeneratedObjectType.Interface

    val isExpectObject
        get() = isObject && isExpect

    val isActualObject: Boolean
        get() = isObject && isActual

    val isExpectInterface
        get() = isInterface && isExpect

    val isActualInterface: Boolean
        get() = isInterface && isActual

    val isTargetObject: Boolean
        get() = isObject && modifier == GeneratedObjectModifier.None

    val isTargetInterface: Boolean
        get() = isInterface && modifier == GeneratedObjectModifier.None
}
