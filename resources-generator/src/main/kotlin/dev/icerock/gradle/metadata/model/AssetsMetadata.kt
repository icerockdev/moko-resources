package dev.icerock.gradle.metadata.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AssetsMetadata(
    @SerialName("type")
    val type: AssetsType,
    val name: String,
    val path: String?,
    val files: List<String>?,
    val assets: List<AssetsMetadata>?,
)

enum class AssetsType {
    File,
    Directory
}
