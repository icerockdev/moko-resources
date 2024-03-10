/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.metadata.container

import dev.icerock.gradle.metadata.resource.ResourceMetadata
import dev.icerock.gradle.serialization.ContainerMetadataSerializer
import dev.icerock.gradle.utils.calculateHash
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable(with = ContainerMetadataSerializer::class)
@SerialName("container-metadata")
sealed interface ContainerMetadata {
    val containerType: String
    fun contentHash(): String?
}

@Serializable
@SerialName("object")
internal data class ObjectMetadata(
    @OptIn(ExperimentalSerializationApi::class)
    @EncodeDefault
    override val containerType: String = "object",
    val name: String,
    val resourceType: ResourceType,
    val resources: List<ResourceMetadata>,
) : ContainerMetadata {
    override fun contentHash(): String = resources.mapNotNull { it.contentHash() }.calculateHash()
}

internal enum class ResourceType {
    STRINGS, PLURALS, IMAGES, FONTS, FILES, COLORS, ASSETS
}
