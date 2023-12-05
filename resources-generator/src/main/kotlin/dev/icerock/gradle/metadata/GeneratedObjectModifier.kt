package dev.icerock.gradle.metadata

enum class GeneratedObjectModifier(val value: String) {
    Expect("expect"),
    Actual("actual"),
    None("none");

    companion object {
        private val VALUES = values()
        fun getByValue(value: String): GeneratedObjectModifier {
            return VALUES.firstOrNull { it.value.lowercase() == value.lowercase() }
                ?: throw Exception("Invalid modifier value")
        }
    }
}
