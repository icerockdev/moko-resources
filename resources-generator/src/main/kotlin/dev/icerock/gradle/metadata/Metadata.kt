package dev.icerock.gradle.metadata

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.File

object Metadata {
    private const val DIRECTORY = "generated/moko-resources/metadata"

    fun createOutputMetadata(
        outputMetadataFile: File,
        sourceSetName: String,
        generatedObjects: List<GeneratedObject>,
    ) {
        if (generatedObjects.isEmpty()) return

        val metadataDir = File(outputMetadataFile, DIRECTORY).apply {
            mkdirs()
        }
        val metadataFile = File(metadataDir, "$sourceSetName-metadata.json")
        metadataFile.createNewFile()

        val generatedJson: String = Json.encodeToString(generatedObjects)

        metadataFile.writeText(generatedJson)
    }

    fun readInputMetadata(
        inputMetadataFile: File,
        sourceSetName: String,
    ): List<GeneratedObject> {
        val metadataDir = File(inputMetadataFile, DIRECTORY)
        val inputMetadata = File(metadataDir, "$sourceSetName-metadata.json")
        val bufferedReader: BufferedReader = File(inputMetadata.toURI()).bufferedReader()
        val inputString: String = bufferedReader.use { it.readText() }

        return Json.decodeFromString(inputString)
    }
}
