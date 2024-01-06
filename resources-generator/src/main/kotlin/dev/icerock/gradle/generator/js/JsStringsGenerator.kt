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
//import dev.icerock.gradle.generator.KeyType
//import dev.icerock.gradle.generator.LanguageType
//import dev.icerock.gradle.generator.NOPObjectBodyExtendable
//import dev.icerock.gradle.generator.ObjectBodyExtendable
//import dev.icerock.gradle.generator.StringsGenerator
//import dev.icerock.gradle.generator.js.JsMRGenerator.Companion.SUPPORTED_LOCALES_PROPERTY_NAME
//import dev.icerock.gradle.utils.flatName
//import kotlinx.serialization.json.buildJsonObject
//import kotlinx.serialization.json.put
//import org.gradle.api.file.FileTree
//import java.io.File
//
//class JsStringsGenerator(
//    resourcesFileTree: FileTree,
//    mrClassPackage: String,
//    strictLineBreaks: Boolean
//) : StringsGenerator(
//    resourcesFileTree = resourcesFileTree,
//    strictLineBreaks = strictLineBreaks
//),

//    override fun beforeGenerateResources(
//        objectBuilder: TypeSpec.Builder,
//        languageMap: Map<LanguageType, Map<KeyType, String>>
//    ) {
//        objectBuilder.generateFallbackAndSupportedLanguageProperties(
//            languages = languageMap.keys.toList(),
//            folder = JsMRGenerator.LOCALIZATION_DIR,
//            fallbackFilePropertyName = STRINGS_FALLBACK_FILE_URL_PROPERTY_NAME,
//            fallbackFile = "${flattenClassPackage}_${STRINGS_JSON_NAME}.json",
//            supportedLocalesPropertyName = SUPPORTED_LOCALES_PROPERTY_NAME,
//            getFileNameForLanguage = { language ->
//                "${flattenClassPackage}_${STRINGS_JSON_NAME}${language.jsResourcesSuffix}.json"
//            }
//        )
//        val languageKeys = languageMap[LanguageType.Base].orEmpty().keys
//        val languageKeysList = languageKeys.joinToString { it.replace(".", "_") }
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

//    companion object {
//        const val STRINGS_JSON_NAME = "stringsJson"
//        const val STRINGS_FALLBACK_FILE_URL_PROPERTY_NAME = "stringsFallbackFileUrl"
//    }
//}
