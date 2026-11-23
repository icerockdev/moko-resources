/*
 * Copyright 2026 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.io.File

/**
 * Serializes [File] relative to [baseDir], so metadata written in one checkout stays valid
 * when a build cache entry is restored into a checkout at another location.
 *
 * Files inside [baseDir] are written as relative paths with `/` separators. Files outside of it
 * are written as absolute paths, which [File.resolve] keeps as is on read.
 */
internal class RelativeFileSerializer(
    private val baseDir: File
) : KSerializer<File> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        serialName = "RelativeFileSerializer",
        kind = PrimitiveKind.STRING
    )

    override fun deserialize(decoder: Decoder): File {
        return baseDir.resolve(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: File) {
        val path: String = if (value.startsWith(baseDir)) {
            value.relativeTo(baseDir).invariantSeparatorsPath
        } else {
            value.path
        }
        encoder.encodeString(path)
    }
}
