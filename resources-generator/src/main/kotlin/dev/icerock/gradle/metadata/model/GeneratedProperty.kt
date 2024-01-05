//package dev.icerock.gradle.metadata.model
//
//import dev.icerock.gradle.metadata.model.GeneratedObjectModifier.Actual
//import kotlinx.serialization.Serializable
//import kotlinx.serialization.json.JsonElement
//
//@Serializable
//data class GeneratedProperty(
//    val modifier: GeneratedObjectModifier,
//    val name: String,
//    val data: JsonElement // Can contain data with dependencies from generator type
//){
//    val isActualProperty: Boolean
//        get() = modifier == Actual
//}
