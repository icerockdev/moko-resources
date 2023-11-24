package dev.icerock.gradle.metadata

enum class GeneratedObjectModifier(val value: String) {
    EXPECT("expect"),
    ACTUAL("actual");

    companion object {
        private val VALUES = values()
        fun getByValue(value: String): GeneratedObjectModifier {
            return VALUES.firstOrNull { it.value.lowercase() == value.lowercase() }
                ?: throw Exception("Invalid modifier value")
        }
    }
}
