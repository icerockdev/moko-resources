//package dev.icerock.gradle.metadata.model
//
//import dev.icerock.gradle.metadata.model.GeneratedObjectModifier.Actual
//import dev.icerock.gradle.metadata.model.GeneratedObjectModifier.Expect
//import dev.icerock.gradle.metadata.model.GeneratedObjectModifier.None
//import dev.icerock.gradle.metadata.model.GeneratedObjectType.Interface
//import dev.icerock.gradle.metadata.model.GeneratedObjectType.Object
//import kotlinx.serialization.Serializable
//
//@Serializable
//data class GeneratedObject(
//    val generatorType: GeneratorType,
//    val modifier: GeneratedObjectModifier,
//    val type: GeneratedObjectType,
//    val name: String,
//    val interfaces: List<String> = emptyList(),
//    val properties: List<GeneratedProperty> = emptyList(),
//    val objects: List<GeneratedObject> = emptyList()
//) {
//    val isExpect: Boolean
//        get() = modifier == Expect
//
//    val isActual: Boolean
//        get() = modifier == Actual
//
//    val isObject: Boolean
//        get() = type == Object
//
//    val isInterface: Boolean
//        get() = type == Interface
//
//    val isExpectObject
//        get() = isObject && isExpect
//
//    val isActualObject: Boolean
//        get() = isObject && isActual
//
//    val isExpectInterface
//        get() = isInterface && isExpect
//
//    val isActualInterface: Boolean
//        get() = isInterface && isActual
//
//    val isTargetObject: Boolean
//        get() = isObject && modifier == None
//
//    val isTargetInterface: Boolean
//        get() = isInterface && modifier == None
//}
