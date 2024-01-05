///*
// * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
// */
//
//package dev.icerock.gradle.generator.android
//
//import com.squareup.kotlinpoet.CodeBlock
//import com.squareup.kotlinpoet.KModifier
//import dev.icerock.gradle.generator.AssetsGenerator
//import dev.icerock.gradle.generator.AssetsGenerator.AssetSpec.AssetSpecDirectory
//import dev.icerock.gradle.generator.AssetsGenerator.AssetSpec.AssetSpecFile
//import dev.icerock.gradle.generator.NOPObjectBodyExtendable
//import dev.icerock.gradle.generator.ObjectBodyExtendable
//import org.gradle.api.file.FileTree
//import java.io.File
//
//class AndroidAssetsGenerator(
//    ownResourcesFileTree: FileTree,
//) : AssetsGenerator(ownResourcesFileTree), ObjectBodyExtendable by NOPObjectBodyExtendable() {
//
//    override fun generateResources(
//        assetsGenerationDir: File,
//        resourcesGenerationDir: File,
//        files: List<AssetSpec>
//    ) {
//        files.forEach { assetSpec ->
//            when (assetSpec) {
//                is AssetSpecDirectory ->
//                    generateResources(assetsGenerationDir, resourcesGenerationDir, assetSpec.assets)
//                is AssetSpecFile ->
//                    assetSpec.file.copyTo(File(assetsGenerationDir, assetSpec.pathRelativeToBase))
//            }
//        }
//    }
//
//    override fun getClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)
//
//    override fun getPropertyModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)
//
//    override fun getPropertyInitializer(fileSpec: AssetSpecFile) =
//        CodeBlock.of("AssetResource(path = %S)", fileSpec.pathRelativeToBase)
//}
