/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.resources.image

import com.android.ide.common.vectordrawable.Svg2Vector
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec.Builder
import dev.icerock.gradle.generator.Constants
import dev.icerock.gradle.generator.PlatformResourceGenerator
import dev.icerock.gradle.generator.addEmptyPlatformResourceProperty
import dev.icerock.gradle.generator.addValuesFunction
import dev.icerock.gradle.metadata.resource.ImageMetadata
import org.slf4j.Logger
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.nio.file.Path
import kotlin.reflect.full.functions

internal class AndroidImageResourceGenerator(
    private val androidRClassPackage: String,
    private val resourcesGenerationDir: File,
    private val logger: Logger,
) : PlatformResourceGenerator<ImageMetadata> {
    override fun imports(): List<ClassName> = listOf(
        ClassName(androidRClassPackage, "R")
    )

    override fun generateInitializer(metadata: ImageMetadata): CodeBlock {
        return CodeBlock.of("ImageResource(R.drawable.%L)", processKey(metadata.key))
    }

    override fun generateBeforeProperties(
        parentObjectName: String,
        builder: Builder,
        metadata: List<ImageMetadata>,
        modifier: KModifier?,
    ) {
        builder.addEmptyPlatformResourceProperty(modifier)
    }

    override fun generateAfterProperties(
        builder: Builder,
        metadata: List<ImageMetadata>,
        modifier: KModifier?,
    ) {
        builder.addValuesFunction(
            metadata = metadata,
            classType = Constants.imageResourceName,
            modifier = modifier
        )
    }

    override fun generateResourceFiles(data: List<ImageMetadata>) {
        data.flatMap { imageMetadata ->
            imageMetadata.values.map { imageMetadata.key to it }
        }.forEach { (key: String, item: ImageMetadata.ImageQualityItem) ->
            val drawableDirName: String = "drawable" + when (item.quality) {
                "0.75" -> "-ldpi"
                "1" -> "-mdpi"
                "1.5" -> "-hdpi"
                "2" -> "-xhdpi"
                "3" -> "-xxhdpi"
                "4" -> "-xxxhdpi"
                null -> ""
                else -> {
                    logger.warn("ignore $item for android - unknown scale (${item.quality})")
                    return@forEach
                }
            }

            val drawableDir = File(resourcesGenerationDir, drawableDirName)
            val processedKey: String = processKey(key)

            val resourceExtension: String = if (item.quality == null) {
                "xml"
            } else {
                item.filePath.extension
            }

            val resourceFile = File(drawableDir, "$processedKey.$resourceExtension")
            if (item.quality == null) {
                parseSvgToVectorDrawable(item.filePath, resourceFile)
            } else {
                item.filePath.copyTo(resourceFile)
            }
        }
    }

    private fun parseSvgToVectorDrawable(svgFile: File, vectorDrawableFile: File) {
        try {
            vectorDrawableFile.parentFile.mkdirs()
            vectorDrawableFile.createNewFile()
            FileOutputStream(vectorDrawableFile, false).use { os ->
                parseSvgToXml(svgFile, os)
                    .takeIf { it.isNotEmpty() }
                    ?.let { error -> logger.warn("parse from $svgFile to xml:\n$error") }
            }
        } catch (e: IOException) {
            logger.error("parse from $svgFile to xml error", e)
        }
    }

    private fun parseSvgToXml(file: File, os: OutputStream): String {
        return try {
            Svg2Vector.parseSvgToXml(Path.of(file.absolutePath), os)
        } catch (e: NoSuchMethodError) {
            logger.debug(
                buildString {
                    append("Not found parseSvgToXml function with Path parameter. ")
                    append("Fallback to parseSvgToXml function with File parameter.")
                },
                e
            )
            val parseSvgToXmlFunction = Svg2Vector::class.functions.first {
                // broken ktlint rule Indentation workaround
                if (it.name != "parseSvgToXml") return@first false
                if (it.parameters.size != 2) return@first false
                if (it.parameters[0].type.classifier != File::class) return@first false
                if (it.parameters[1].type.classifier != OutputStream::class) return@first false
                return@first true
            }
            return parseSvgToXmlFunction.call(file, os) as String
        }
    }

    private fun processKey(key: String): String {
        return key.lowercase()
    }
}
