package dev.icerock.gradle.generator.metadata.container

import dev.icerock.gradle.metadata.container.ContainerMetadata
import dev.icerock.gradle.metadata.container.ExpectInterfaceMetadata
import dev.icerock.gradle.metadata.container.ObjectMetadata
import dev.icerock.gradle.metadata.container.ResourceType
import dev.icerock.gradle.metadata.resource.StringMetadata
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals

class ContainerMetadataTest {

    @Test
    fun testSerialization() {
        val expectInter = ExpectInterfaceMetadata(
            name = "test2",
            resourceType = ResourceType.STRINGS,
            sourceSet = "testing"
        )
        val obj = ObjectMetadata(
            name = "test",
            resourceType = ResourceType.STRINGS,
            resources = listOf(
                StringMetadata(
                    key = "hello",
                    values = listOf(
                        StringMetadata.LocaleItem(locale = "ru", value = "ru"),
                        StringMetadata.LocaleItem(locale = "en", value = "en")
                    )
                )
            ),
            interfaces = listOf("test1", "test2")
        )
        val serializer = ListSerializer(ContainerMetadata.serializer())

        val input: List<ContainerMetadata> = listOf(expectInter, obj)
        val json: String = Json.encodeToString(serializer, input)
        val list: List<ContainerMetadata> = Json.decodeFromString(serializer, json)

        assertEquals(expected = input, actual = list)
    }
}
