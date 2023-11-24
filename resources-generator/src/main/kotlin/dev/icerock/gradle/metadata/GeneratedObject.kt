package dev.icerock.gradle.metadata

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.jetbrains.kotlin.com.google.gson.annotations.SerializedName

data class GeneratedObject(
    val type: GeneratedObjectType,
    val name: String,
    val modifier: GeneratedObjectModifier,
    val variables: List<GeneratedVariables>,
) {
    val objectSpec: String
        get() = "${modifier.value} ${type.value} $name"

    val asJsonObject: JsonObject
        get() = buildJsonObject {
            put(key = "name", value = name)
            put(key = "type", value = type.value)
            put(key = "modifier", value = modifier.value)
            put(
                key = "variables",
                element = buildJsonArray {
                    variables.forEach {
                        add(it.asJsonObject)
                    }
                }
            )
        }
}

data class GeneratedObjectMetadata(
    @SerializedName("type")
    val type: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("modifier")
    val modifier: String,
    @SerializedName("variables")
    val variables: List<GeneratedVariablesMetadata>,
) {
    val asGeneratedObject: GeneratedObject
        get() {
            return GeneratedObject(
                type = GeneratedObjectType.getByValue(type),
                name = name,
                modifier = GeneratedObjectModifier.getByValue(modifier),
                variables = variables.map {
                    it.asGeneratedVariables
                }
            )
        }
}