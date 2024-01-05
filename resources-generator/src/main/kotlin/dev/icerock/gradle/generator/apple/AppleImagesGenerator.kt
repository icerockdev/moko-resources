///*
// * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
// */
//
//package dev.icerock.gradle.generator.apple
//
//import com.squareup.kotlinpoet.CodeBlock
//import com.squareup.kotlinpoet.KModifier
//import dev.icerock.gradle.generator.ImagesGenerator
//import dev.icerock.gradle.generator.ObjectBodyExtendable
//import dev.icerock.gradle.generator.apple.AppleMRGenerator.Companion.ASSETS_DIR_NAME
//import dev.icerock.gradle.utils.nameWithoutScale
//import dev.icerock.gradle.utils.scale
//import dev.icerock.gradle.utils.svg
//import kotlinx.serialization.json.JsonPrimitive
//import kotlinx.serialization.json.buildJsonArray
//import kotlinx.serialization.json.buildJsonObject
//import org.gradle.api.file.FileTree
//import java.io.File
//
//class AppleImagesGenerator(
//    ownInputFileTree: FileTree,
//) : ImagesGenerator(resourcesFileTree = ownInputFileTree), ObjectBodyExtendable by AppleGeneratorHelper() {
//
//    override fun getClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)
//
//    override fun getPropertyModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)
//
//    override fun getPropertyInitializer(fileName: String): CodeBlock? {
//        return CodeBlock.of(
//            "ImageResource(assetImageName = %S, bundle = ${AppleMRGenerator.BUNDLE_PROPERTY_NAME})",
//            fileName.substringBefore(".")
//        )
//    }
//
//    override fun generateResources(
//        assetsGenerationDir: File,
//        resourcesGenerationDir: File,
//        keyFileMap: Map<String, List<File>>
//    ) {
//        val assetsDirectory = File(assetsGenerationDir, ASSETS_DIR_NAME)
//
//        keyFileMap.forEach { (key, files) ->
//            val assetDir = File(assetsDirectory, "$key.imageset")
//            val contentsFile = File(assetDir, "Contents.json")
//
//            val validFiles = files.filter { file ->
//                file.svg || VALID_SIZES.any { size -> file.scale == "${size}x" }
//            }
//
//            val uniqueNames = files.map { it.nameWithoutScale }.distinct()
//            uniqueNames.forEach { name ->
//                require(validFiles.any { it.nameWithoutScale == name }) {
//                    buildString {
//                        appendLine("Apple Generator cannot find a valid scale for file with name \"$name\".")
//                        append("Note: Apple resources can have only 1x, 2x and 3x scale factors ")
//                        append("(https://developer.apple.com/design/human-interface-guidelines/ios/")
//                        appendLine("icons-and-images/image-size-and-resolution/).")
//                        append("It is still possible to use 4x images for android, but you need to ")
//                        append("add a valid iOS variant.")
//                    }
//                }
//            }
//
//            validFiles.forEach { it.copyTo(File(assetDir, it.name)) }
//
//            val imagesContent = buildJsonArray {
//                validFiles.map { file ->
//                    buildJsonObject {
//                        put("idiom", JsonPrimitive("universal"))
//                        put("filename", JsonPrimitive(file.name))
//                        if (!file.svg) {
//                            put("scale", JsonPrimitive(file.scale))
//                        }
//                    }
//                }.forEach { add(it) }
//            }
//
//            val content = buildJsonObject {
//                put("images", imagesContent)
//                put(
//                    "info",
//                    buildJsonObject {
//                        put("version", JsonPrimitive(1))
//                        put("author", JsonPrimitive("xcode"))
//                    }
//                )
//
//                if (validFiles.any { file -> file.svg }) {
//                    put(
//                        "properties",
//                        buildJsonObject {
//                            put("preserves-vector-representation", JsonPrimitive(true))
//                        }
//                    )
//                }
//            }.toString()
//
//            contentsFile.writeText(content)
//        }
//    }
//
//    private companion object {
//        val VALID_SIZES: IntRange = 1..3
//    }
//}
