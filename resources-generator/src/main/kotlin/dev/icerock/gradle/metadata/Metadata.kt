package dev.icerock.gradle.metadata

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.BufferedReader
import java.io.File

object Metadata {
    private const val DIRECTORY = "generated/moko-resources/metadata"

    fun createOutputMetadata(
        buildDir: File,
        sourceSetName: String,
        generatedObjects: List<GeneratedObject>,
    ) {
        if (generatedObjects.isEmpty()) return

        val metadataDir = File(buildDir, DIRECTORY).apply {
            mkdirs()
        }
        val metadataFile = File(metadataDir, "$sourceSetName-metadata.json")
        metadataFile.createNewFile()

        val generatedJson: JsonArray = buildJsonArray {
            generatedObjects.forEach { generatedObject ->
                add(generatedObject.asJsonObject)
            }
        }

        metadataFile.writeText(generatedJson.toString())
    }

    fun readInputMetadata(
        buildDir: File,
        sourceSetName: String,
    ): List<GeneratedObject> {
        val metadataDir = File(buildDir, DIRECTORY)
        val inputMetadata = File(metadataDir, "$sourceSetName-metadata.json")
        val bufferedReader: BufferedReader = File(inputMetadata.toURI()).bufferedReader()
        val inputString: String = bufferedReader.use { it.readText() }

        val generatedObjects = mutableListOf<GeneratedObject>()

        val parsedArray: JsonArray = Json.parseToJsonElement(inputString).jsonArray

        parsedArray.forEach { generatedObjectJson: JsonElement ->
            val element: JsonObject = generatedObjectJson.jsonObject
            val name: String = element.getValue(GeneratedObject.KEY_NAME)
            val type: String = element.getValue(GeneratedObject.KEY_TYPE)
            val modifier: String = element.getValue(GeneratedObject.KEY_MODIFIER)
            val variables: JsonArray? = element[GeneratedObject.KEY_PROPERTIES]?.jsonArray

            generatedObjects.add(
                GeneratedObject(
                    name = name,
                    type = GeneratedObjectType.getByValue(type),
                    modifier = GeneratedObjectModifier.getByValue(modifier),
                    properties = variables?.map { variableJson ->
                        val variableElement: JsonObject = variableJson.jsonObject
                        val variableName: String = variableElement.getValue(
                            key = GeneratedProperties.KEY_NAME
                        )
                        val variableModifier: String = variableElement.getValue(
                            key = GeneratedProperties.KEY_MODIFIER
                        )

                        GeneratedProperties(
                            name = variableName,
                            modifier = GeneratedObjectModifier.getByValue(variableModifier)

                        )
                    }.orEmpty()
                )
            )
        }

        return generatedObjects
    }

    private fun JsonObject.getValue(key: String): String {
        return this[key]?.jsonPrimitive?.content ?: ""
    }
}
