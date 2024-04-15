/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.metadata.container

import dev.icerock.gradle.metadata.container.ContainerMetadata
import dev.icerock.gradle.metadata.container.ResourceType.STRINGS
import dev.icerock.gradle.metadata.resource.StringMetadata
import dev.icerock.gradle.metadata.resource.StringMetadata.LocaleItem
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals

class ContainerMetadataTest {

    @Test
    fun testSerialization() {
        val obj = ContainerMetadata(
            name = "test",
            parentObjectName = "MR",
            resourceType = STRINGS,
            resources = listOf(
                StringMetadata(
                    key = "hello",
                    values = listOf(
                        LocaleItem(locale = "ru", value = "ru"),
                        LocaleItem(locale = "en", value = "en")
                    )
                )
            ),
        )
        val serializer = ListSerializer(ContainerMetadata.serializer())

        val input: List<ContainerMetadata> = listOf(obj)
        val json: String = Json.encodeToString(serializer, input)
        val list: List<ContainerMetadata> = Json.decodeFromString(serializer, json)

        assertEquals(expected = input, actual = list)
    }
}
