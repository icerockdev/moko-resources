package dev.icerock.gradle.serialization

import dev.icerock.gradle.metadata.container.ActualInterfaceMetadata
import dev.icerock.gradle.metadata.container.ContainerMetadata
import dev.icerock.gradle.metadata.container.ExpectInterfaceMetadata
import dev.icerock.gradle.metadata.container.ObjectMetadata
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.modules.SerializersModule

internal object ContainerMetadataSerializer : KSerializer<ContainerMetadata> {
    private val json by lazy {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
            prettyPrint = true
            coerceInputValues = true

            serializersModule = SerializersModule {
                polymorphic(
                    baseClass = ContainerMetadata::class,
                    actualClass = ExpectInterfaceMetadata::class,
                    actualSerializer = ExpectInterfaceMetadata.serializer()
                )
                polymorphic(
                    baseClass = ContainerMetadata::class,
                    actualClass = ActualInterfaceMetadata::class,
                    actualSerializer = ActualInterfaceMetadata.serializer()
                )
                polymorphic(
                    baseClass = ContainerMetadata::class,
                    actualClass = ObjectMetadata::class,
                    actualSerializer = ObjectMetadata.serializer()
                )
            }
        }
    }

    override val descriptor: SerialDescriptor = PolymorphicSerializer(ContainerMetadata::class).descriptor

    override fun deserialize(decoder: Decoder): ContainerMetadata {
        val jsonElement = (decoder as JsonDecoder).decodeJsonElement()

        return when (val type = jsonElement.jsonObject["containerType"]?.jsonPrimitive?.content) {
            "actual-interface" -> json.decodeFromJsonElement(ActualInterfaceMetadata.serializer(), jsonElement)
            "expect-interface" -> json.decodeFromJsonElement(ExpectInterfaceMetadata.serializer(), jsonElement)
            "object" -> json.decodeFromJsonElement(ObjectMetadata.serializer(), jsonElement)
            else -> throw SerializationException(
                message = "ContainerMetadataSerializer. Unknown type: $type. Element: $jsonElement"
            )
        }
    }

    override fun serialize(encoder: Encoder, value: ContainerMetadata) {
        when (value) {
            is ActualInterfaceMetadata -> {
                ActualInterfaceMetadata.serializer().serialize(encoder, value)
            }
            is ExpectInterfaceMetadata -> {
                ExpectInterfaceMetadata.serializer().serialize(encoder, value)
            }
            is ObjectMetadata -> {
                ObjectMetadata.serializer().serialize(encoder, value)
            }
        }
    }
}
