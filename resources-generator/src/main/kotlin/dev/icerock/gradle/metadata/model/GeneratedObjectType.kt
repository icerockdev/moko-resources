//package dev.icerock.gradle.metadata.model
//
//enum class GeneratedObjectType(val value: String) {
//    Object("object"),
//    Interface("interface");
//
//    companion object {
//        private val VALUES = values()
//        fun getByValue(value: String): GeneratedObjectType {
//            return VALUES.firstOrNull { it.value.lowercase() == value.lowercase() }
//                ?: throw Exception("Invalid object type")
//        }
//    }
//}
