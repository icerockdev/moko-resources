/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.metadata.container

import dev.icerock.gradle.metadata.resource.ResourceMetadata
import dev.icerock.gradle.utils.calculateHash
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("object")
internal data class ContainerMetadata(
    val name: String,
    val parentObjectName: String,
    val resourceType: ResourceType,
    val resources: List<ResourceMetadata>,
) {
    fun contentHash(): String = resources.mapNotNull { it.contentHash() }.calculateHash()
}

internal enum class ResourceType {
    STRINGS, PLURALS, IMAGES, FONTS, FILES, COLORS, ASSETS
}
