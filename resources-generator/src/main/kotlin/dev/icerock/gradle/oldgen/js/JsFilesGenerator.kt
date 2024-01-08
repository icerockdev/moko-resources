///*
// * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
// */
//
//package dev.icerock.gradle.generator.js
//
//import com.squareup.kotlinpoet.ClassName
//import com.squareup.kotlinpoet.CodeBlock
//import com.squareup.kotlinpoet.FunSpec
//import com.squareup.kotlinpoet.KModifier
//import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
//import com.squareup.kotlinpoet.TypeSpec
//import dev.icerock.gradle.generator.FilesGenerator
//import dev.icerock.gradle.generator.NOPObjectBodyExtendable
//import dev.icerock.gradle.generator.ObjectBodyExtendable
//import org.gradle.api.file.FileTree
//import java.io.File
//
//class JsFilesGenerator(
//    ownInputFileTree: FileTree,
//) : FilesGenerator(
//    resourceFiles = ownInputFileTree
//), ObjectBodyExtendable by NOPObjectBodyExtendable() {
//
//    override fun getClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)
//
//    override fun getPropertyModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)
//
//    override fun getPropertyInitializer(fileSpec: FileSpec): CodeBlock {
//        val requireDeclaration = """require("$FILES_DIR/${fileSpec.file.name}")"""
//        return CodeBlock.of(
//            "FileResource(fileUrl = js(%S) as String)",
//            requireDeclaration
//        )
//    }
//
//    override fun beforeGenerate(objectBuilder: TypeSpec.Builder, files: List<FileSpec>) {
//        val languageKeysList = files.joinToString { it.key }
//
//        objectBuilder.addFunction(
//            FunSpec.builder("values")
//                .addModifiers(KModifier.OVERRIDE)
//                .addStatement("return listOf($languageKeysList)")
//                .returns(
//                    ClassName("kotlin.collections", "List")
//                        .parameterizedBy(resourceClassName)
//                )
//                .build()
//        )
//    }
//
//    override fun generateResources(resourcesGenerationDir: File, files: List<FileSpec>) {
//        val fileResDir = File(resourcesGenerationDir, FILES_DIR).apply { mkdirs() }
//        files.forEach { (_, file) ->
//            file.copyTo(File(fileResDir, file.name))
//        }
//    }
//
//    companion object {
//        const val FILES_DIR = "files"
//    }
//}
