/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.serialization

import dev.icerock.gradle.metadata.resource.ColorMetadata.ColorItem
import dev.icerock.gradle.metadata.resource.ColorMetadata.ColorItem.Single
import dev.icerock.gradle.metadata.resource.ColorMetadata.ColorItem.Themed
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

internal object ColorResourceSerializer : KSerializer<ColorItem> {
    private val json by lazy {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
            prettyPrint = true
            coerceInputValues = true

            serializersModule = SerializersModule {
                polymorphic(
                    baseClass = ColorItem::class,
                    actualClass = Single::class,
                    actualSerializer = Single.serializer()
                )
                polymorphic(
                    baseClass = ColorItem::class,
                    actualClass = Themed::class,
                    actualSerializer = Themed.serializer()
                )
            }
        }
    }

    override val descriptor: SerialDescriptor = PolymorphicSerializer(ColorItem::class).descriptor

    override fun deserialize(decoder: Decoder): ColorItem {
        val jsonElement = (decoder as JsonDecoder).decodeJsonElement()

        return when (val type = jsonElement.jsonObject["colorType"]?.jsonPrimitive?.content) {
            Single.getJavaName() -> json.decodeFromJsonElement(Single.serializer(), jsonElement)
            Themed.getJavaName() -> json.decodeFromJsonElement(Themed.serializer(), jsonElement)
            else -> throw SerializationException(
                message = "ColorResourceSerializer. Unknown type: $type. Element: $jsonElement"
            )
        }
    }

    override fun serialize(encoder: Encoder, value: ColorItem) {
        when (value) {
            is Single -> {
                Single.serializer().serialize(encoder, value)
            }
            is Themed -> {
                Themed.serializer().serialize(encoder, value)
            }
        }
    }
}
