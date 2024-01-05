package dev.icerock.gradle.rework.metadata.container

import dev.icerock.gradle.rework.metadata.resource.StringMetadata
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.junit.Test

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

        val json: String = Json.encodeToString(serializer, listOf(expectInter, obj))
        println(json)

        val list: List<ContainerMetadata> = Json.decodeFromString(serializer, json)

        println(list)
    }
}
