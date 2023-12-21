package dev.icerock.gradle.metadata

import dev.icerock.gradle.metadata.model.GeneratedObject
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gradle.api.file.FileTree
import java.io.BufferedReader
import java.io.File

object Metadata {
    fun createOutputMetadata(
        outputMetadataFile: File,
        generatedObjects: List<GeneratedObject>,
    ) {
        if (generatedObjects.isEmpty()) return

        outputMetadataFile.createNewFile()

        val generatedJson: String = Json.encodeToString(generatedObjects)

        outputMetadataFile.writeText(generatedJson)
    }

    fun readInputMetadata(
        inputMetadataFiles: FileTree,
    ): List<GeneratedObject> {
        val generatedObjects = mutableListOf<GeneratedObject>()

        inputMetadataFiles.forEach { inputFile ->
            if(inputFile.isDirectory) return@forEach

            val bufferedReader: BufferedReader = File(inputFile.toURI()).bufferedReader()
            val inputString: String = bufferedReader.use { it.readText() }
            val inputMetadata: List<GeneratedObject> = Json.decodeFromString(inputString)
            generatedObjects.addAll(inputMetadata)
        }

        return generatedObjects
    }
}
