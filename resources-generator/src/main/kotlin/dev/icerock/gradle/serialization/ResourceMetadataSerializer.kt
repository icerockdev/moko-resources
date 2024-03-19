package dev.icerock.gradle.serialization

import dev.icerock.gradle.metadata.resource.AssetMetadata
import dev.icerock.gradle.metadata.resource.ColorMetadata
import dev.icerock.gradle.metadata.resource.FileMetadata
import dev.icerock.gradle.metadata.resource.FontMetadata
import dev.icerock.gradle.metadata.resource.ImageMetadata
import dev.icerock.gradle.metadata.resource.PluralMetadata
import dev.icerock.gradle.metadata.resource.ResourceMetadata
import dev.icerock.gradle.metadata.resource.StringMetadata
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.modules.SerializersModule

internal object ResourceMetadataSerializer : KSerializer<ResourceMetadata> {
    private val json by lazy {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
            prettyPrint = true
            coerceInputValues = true

            serializersModule = SerializersModule {
                polymorphic(
                    baseClass = ResourceMetadata::class,
                    actualClass = AssetMetadata::class,
                    actualSerializer = AssetMetadata.serializer()
                )
                polymorphic(
                    baseClass = ResourceMetadata::class,
                    actualClass = ColorMetadata::class,
                    actualSerializer = ColorMetadata.serializer()
                )
                polymorphic(
                    baseClass = ResourceMetadata::class,
                    actualClass = FileMetadata::class,
                    actualSerializer = FileMetadata.serializer()
                )
                polymorphic(
                    baseClass = ResourceMetadata::class,
                    actualClass = FontMetadata::class,
                    actualSerializer = FontMetadata.serializer()
                )
                polymorphic(
                    baseClass = ResourceMetadata::class,
                    actualClass = ImageMetadata::class,
                    actualSerializer = ImageMetadata.serializer()
                )
                polymorphic(
                    baseClass = ResourceMetadata::class,
                    actualClass = PluralMetadata::class,
                    actualSerializer = PluralMetadata.serializer()
                )
                polymorphic(
                    baseClass = ResourceMetadata::class,
                    actualClass = StringMetadata::class,
                    actualSerializer = StringMetadata.serializer()
                )
            }
        }
    }

    override val descriptor: SerialDescriptor =
        PolymorphicSerializer(ResourceMetadata::class).descriptor

    override fun deserialize(decoder: Decoder): ResourceMetadata {
        val jsonElement = (decoder as JsonDecoder).decodeJsonElement()

        return when (val type = jsonElement.jsonObject["resourceType"]?.jsonPrimitive?.content) {
            AssetMetadata::class.java.name -> json.decodeFromJsonElement(
                deserializer = AssetMetadata.serializer(),
                element = jsonElement
            )
            ColorMetadata::class.java.name -> json.decodeFromJsonElement(
                deserializer = ColorMetadata.serializer(),
                element = jsonElement
            )
            FileMetadata::class.java.name -> json.decodeFromJsonElement(
                deserializer = FileMetadata.serializer(),
                element = jsonElement
            )
            FontMetadata::class.java.name -> json.decodeFromJsonElement(
                deserializer = FontMetadata.serializer(),
                element = jsonElement
            )
            ImageMetadata::class.java.name -> json.decodeFromJsonElement(
                deserializer = ImageMetadata.serializer(),
                element = jsonElement
            )
            PluralMetadata::class.java.name -> json.decodeFromJsonElement(
                deserializer = PluralMetadata.serializer(),
                element = jsonElement
            )
            StringMetadata::class.java.name -> json.decodeFromJsonElement(
                deserializer = StringMetadata.serializer(),
                element = jsonElement
            )
            else -> throw SerializationException(
                message = "ResourceMetadataSerializer. Unknown type: $type. Element: $jsonElement"
            )
        }
    }

    override fun serialize(encoder: Encoder, value: ResourceMetadata) {
        encoder as JsonEncoder
        when (value) {
            is AssetMetadata -> {
                encoder.encodeSerializableValue(AssetMetadata.serializer(), value)
            }
            is ColorMetadata -> {
                encoder.encodeSerializableValue(ColorMetadata.serializer(), value)
            }
            is FileMetadata -> {
                encoder.encodeSerializableValue(FileMetadata.serializer(), value)
            }
            is FontMetadata -> {
                encoder.encodeSerializableValue(FontMetadata.serializer(), value)
            }
            is ImageMetadata -> {
                encoder.encodeSerializableValue(ImageMetadata.serializer(), value)
            }
            is PluralMetadata -> {
                encoder.encodeSerializableValue(PluralMetadata.serializer(), value)
            }
            is StringMetadata -> {
                encoder.encodeSerializableValue(StringMetadata.serializer(), value)
            }
        }
    }
}
