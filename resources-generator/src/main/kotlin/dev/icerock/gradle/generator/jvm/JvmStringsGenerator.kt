///*
// * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
// */
//
//package dev.icerock.gradle.generator.jvm
//
//import com.squareup.kotlinpoet.CodeBlock
//import com.squareup.kotlinpoet.KModifier
//import dev.icerock.gradle.generator.KeyType
//import dev.icerock.gradle.generator.LanguageType
//import dev.icerock.gradle.generator.MRGenerator
//import dev.icerock.gradle.generator.ObjectBodyExtendable
//import dev.icerock.gradle.generator.StringsGenerator
//import dev.icerock.gradle.utils.flatName
//import org.gradle.api.file.FileTree
//import java.io.File
//
//class JvmStringsGenerator(
//    resourcesFileTree: FileTree,
//    strictLineBreaks: Boolean,
//    settings: MRGenerator.Settings,
//) : StringsGenerator(
//    resourcesFileTree = resourcesFileTree,
//    strictLineBreaks = strictLineBreaks
//), ObjectBodyExtendable by ClassLoaderExtender(settings.className) {
//
//    private val flattenClassPackage = settings.packageName.flatName
//
//    override fun getClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)
//
//    override fun getPropertyModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)
//
//    override fun getPropertyInitializer(key: String) =
//        CodeBlock.of(
//            "StringResource(resourcesClassLoader = resourcesClassLoader, bundleName = %L, key = %S)",
//            JvmMRGenerator.STRINGS_BUNDLE_PROPERTY_NAME,
//            key
//        )
//
//    override fun generateResources(
//        resourcesGenerationDir: File,
//        language: LanguageType,
//        strings: Map<KeyType, String>,
//    ) {
//        val fileDirName =
//            "${flattenClassPackage}_${JvmMRGenerator.STRINGS_BUNDLE_NAME}${language.jvmResourcesSuffix}"
//
//        val localizationDir = File(resourcesGenerationDir, JvmMRGenerator.LOCALIZATION_DIR).apply {
//            mkdirs()
//        }
//        val stringsFile = File(localizationDir, "$fileDirName.properties")
//
//        val content = strings.map { (key, value) ->
//            "$key = ${convertXmlStringToJvmLocalization(value)}"
//        }.joinToString("\n")
//
//        stringsFile.writeText(content)
//    }
//}
