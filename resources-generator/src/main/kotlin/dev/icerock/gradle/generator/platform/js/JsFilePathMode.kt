package dev.icerock.gradle.generator.platform.js

internal data class JsFilePathMode(
    val format: String,
    val argument: (String) -> String,
) {
    companion object {
        val require = JsFilePathMode(
            format = "js(%S)",
            argument = { """require("$it")""" }
        )
        val rawPath = JsFilePathMode(
            format = "%S",
            argument = { it }
        )
    }
}
