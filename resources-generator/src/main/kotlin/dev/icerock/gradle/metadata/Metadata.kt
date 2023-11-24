package dev.icerock.gradle.metadata

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.buildJsonArray
import java.io.BufferedReader
import java.io.File

object Metadata {
    private const val DIRECTORY = "generated/moko-resources/metadata"

    fun createOutputMetadata(
        buildDir: File,
        sourceSetName: String,
        generatedObjects: List<GeneratedObject>,
    ) {
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
        val metadataDir = File(buildDir, DIRECTORY).apply {
            mkdirs()
        }
        val inputMetadata = File(metadataDir, "$sourceSetName-metadata.json")
        val bufferedReader: BufferedReader = File(inputMetadata.toURI()).bufferedReader()
        val inputString = bufferedReader.use { it.readText() }

        return Json.decodeFromString<List<GeneratedObjectMetadata>>(inputString).map {
            it.asGeneratedObject
        }
    }
}
