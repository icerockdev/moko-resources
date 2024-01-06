/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.rework.metadata.container

import dev.icerock.gradle.rework.metadata.resource.ResourceMetadata
import dev.icerock.gradle.utils.calculateHash
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface ContainerMetadata {
    fun contentHash(): String?
}

@Serializable
@SerialName("expect-interface")
data class ExpectInterfaceMetadata(
    val name: String,
    val resourceType: ResourceType,
    val sourceSet: String
) : ContainerMetadata {
    override fun contentHash() = null
}

@Serializable
@SerialName("actual-interface")
data class ActualInterfaceMetadata(
    val name: String,
    val resources: List<ResourceMetadata>
) : ContainerMetadata {
    override fun contentHash(): String = resources.mapNotNull { it.contentHash() }.calculateHash()
}

@Serializable
@SerialName("object")
data class ObjectMetadata(
    val name: String,
    val resourceType: ResourceType,
    val interfaces: List<String>,
    val resources: List<ResourceMetadata>
) : ContainerMetadata {

    override fun contentHash(): String = resources.mapNotNull { it.contentHash() }.calculateHash()
}

enum class ResourceType {
    STRINGS, PLURALS, IMAGES, FONTS, FILES, COLORS, ASSETS
}
