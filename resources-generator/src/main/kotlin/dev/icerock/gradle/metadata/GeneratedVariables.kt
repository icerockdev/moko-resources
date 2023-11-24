package dev.icerock.gradle.metadata

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.jetbrains.kotlin.com.google.gson.annotations.SerializedName

data class GeneratedVariables(
    val name: String,
    val modifier: GeneratedObjectModifier,
) {
    val asJsonObject: JsonObject
        get() = buildJsonObject {
            put("name", name)
            put("modifier", modifier.value)
        }
}

data class GeneratedVariablesMetadata(
    @SerializedName("name")
    val name: String,
    @SerializedName("modifier")
    val modifier: String,
) {
    val asGeneratedVariables: GeneratedVariables
        get() {
            return GeneratedVariables(
                name = name,
                modifier = GeneratedObjectModifier.getByValue(modifier)
            )
        }
}
