//package dev.icerock.gradle.metadata
//
//import dev.icerock.gradle.metadata.model.GeneratedObject
//import kotlinx.serialization.builtins.ListSerializer
//import kotlinx.serialization.json.Json
//import org.gradle.api.file.FileTree
//import java.io.File
//
//object Metadata {
//    fun createOutputMetadata(
//        outputMetadataFile: File,
//        generatedObjects: List<GeneratedObject>,
//    ) {
//        if (generatedObjects.isEmpty()) return
//
//        outputMetadataFile.createNewFile()
//
//        val generatedJson: String = Json.encodeToString(
//            serializer = ListSerializer(GeneratedObject.serializer()),
//            value = generatedObjects
//        )
//
//        outputMetadataFile.writeText(generatedJson)
//    }
//
//    fun readInputMetadata(
//        inputMetadataFiles: FileTree,
//    ): List<GeneratedObject> {
//        return inputMetadataFiles.filter { it.isFile }.flatMap { inputFile ->
//            val inputString: String = inputFile.readText()
//            Json.decodeFromString(
//                deserializer = ListSerializer(GeneratedObject.serializer()),
//                string = inputString
//            )
//        }
//    }
//}
