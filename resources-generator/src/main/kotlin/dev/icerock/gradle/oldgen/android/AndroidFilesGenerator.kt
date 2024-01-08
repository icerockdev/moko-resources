///*
// * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
// */
//
//package dev.icerock.gradle.generator.android
//
//import com.squareup.kotlinpoet.ClassName
//import com.squareup.kotlinpoet.CodeBlock
//import com.squareup.kotlinpoet.KModifier
//import dev.icerock.gradle.generator.FilesGenerator
//import dev.icerock.gradle.generator.NOPObjectBodyExtendable
//import dev.icerock.gradle.generator.ObjectBodyExtendable
//import org.gradle.api.file.FileTree
//import org.gradle.api.provider.Provider
//import java.io.File
//import java.util.Locale
//
//class AndroidFilesGenerator(
//    ownInputFileTree: FileTree,
//    private val androidRClassPackage: Provider<String>,
//) : FilesGenerator(ownInputFileTree), ObjectBodyExtendable by NOPObjectBodyExtendable() {
//    override fun getClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)
//
//    override fun getPropertyModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)
//
//    override fun getPropertyInitializer(fileSpec: FileSpec) =
//        CodeBlock.of("FileResource(rawResId = R.raw.%L)", keyToResourceId(fileSpec.key))
//
//    override fun getImports() = listOf(
//        ClassName(androidRClassPackage.get(), "R")
//    )
//
//    override fun generateResources(
//        resourcesGenerationDir: File,
//        files: List<FileSpec>
//    ) {
//        val targetDir = File(resourcesGenerationDir, "raw")
//        targetDir.mkdirs()
//
//        files.forEach { fileSpec ->
//            val fileName = keyToResourceId(fileSpec.key) + "." + fileSpec.file.extension
//            fileSpec.file.copyTo(File(targetDir, fileName))
//        }
//    }
//
//    private fun keyToResourceId(key: String) = key.lowercase(Locale.ROOT)
//}
