/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

@file:UseSerializers(FileSerializer::class)

package dev.icerock.gradle.metadata.resource

import dev.icerock.gradle.serialization.FileSerializer
import dev.icerock.gradle.utils.calculateHash
import dev.icerock.gradle.utils.calculateResourcesHash
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.io.File

@Serializable
internal sealed interface ResourceMetadata {
    val key: String

    fun contentHash(): String?
}

@Serializable
internal data class StringMetadata(
    override val key: String,
    val values: List<LocaleItem>
) : ResourceMetadata {
    @Serializable
    data class LocaleItem(
        val locale: String,
        val value: String
    )

    override fun contentHash(): String = values.hashCode().toString(16)
}

@Serializable
internal data class PluralMetadata(
    override val key: String,
    val values: List<LocaleItem>
) : ResourceMetadata {
    @Serializable
    data class LocaleItem(
        val locale: String,
        val values: List<PluralItem>
    )

    @Serializable
    data class PluralItem(
        val quantity: Quantity,
        val value: String
    ) {
        enum class Quantity {
            ZERO, ONE, TWO, FEW, MANY, OTHER;
        }
    }

    override fun contentHash(): String = values.hashCode().toString(16)
}

@Serializable
internal data class ImageMetadata(
    override val key: String,
    val values: List<ImageQualityItem>
) : ResourceMetadata {
    @Serializable
    data class ImageQualityItem(
        val quality: Int,
        val filePath: File
    )

    override fun contentHash(): String = values.map { it.filePath.calculateResourcesHash() }
        .calculateHash()
}

@Serializable
internal data class FontMetadata(
    override val key: String,
    val values: List<FontFamilyItem>
) : ResourceMetadata {
    @Serializable
    data class FontFamilyItem(
        val family: String,
        val filePath: File
    )

    override fun contentHash(): String = values.map { it.filePath.calculateResourcesHash() }
        .calculateHash()
}

@Serializable
internal data class FileMetadata(
    override val key: String,
    val filePath: File
) : ResourceMetadata {

    override fun contentHash(): String = filePath.calculateResourcesHash()
}

@Serializable
internal data class ColorMetadata(
    override val key: String,
    val value: ColorItem
) : ResourceMetadata {
    @Serializable
    sealed interface ColorItem {
        @Serializable
        data class Single(val color: Color) : ColorItem

        @Serializable
        data class Themed(val light: Color, val dark: Color) : ColorItem

        @Serializable
        data class Reference(val key: String) : ColorItem
    }

    @Serializable
    data class Color(
        val red: Int,
        val green: Int,
        val blue: Int,
        val alpha: Int
    )

    override fun contentHash(): String = value.hashCode().toString(16)
}

@Serializable
internal data class AssetsMetadata(
    override val key: String,
    val relativePath: File,
    val filePath: File
) : ResourceMetadata {
    override fun contentHash(): String = filePath.calculateResourcesHash()
}
