/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.js

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.generator.LanguageType

/**
 * @param folder the folder, where the localization files are in.
 * @param languages the language codes supported
 * @param fallbackFilePropertyName the property name in the MR object
 * @param fallbackFile the name of the file webpack will use, e.g. my_plurals.json
 */
fun TypeSpec.Builder.generateFallbackAndSupportedLanguageProperties(
    languages: List<LanguageType>,
    folder: String,
    fallbackFilePropertyName: String,
    fallbackFile: String,
    supportedLocalesPropertyName: String,
    getFileNameForLanguage: (language: String) -> String
) {
    if (languages.isEmpty()) return

    addProperty(
        PropertySpec
            .builder(fallbackFilePropertyName, String::class, KModifier.PRIVATE)
            .initializer(
                CodeBlock.of(
                    "js(%S) as %T",
                    "require(\"$folder/$fallbackFile\")",
                    String::class
                )
            )
            .build()
    )

    val internalPackage = "dev.icerock.moko.resources.internal"
    val supportedLocalesName = ClassName(internalPackage, "SupportedLocales")
    val supportedLocaleName = ClassName(internalPackage, "SupportedLocale")
    val localizedStringLoaderName = ClassName(internalPackage, "LocalizedStringLoader")
    val loaderName = ClassName(internalPackage, "LocalizedStringLoaderHolder")

    addProperty(
        PropertySpec
            .builder(supportedLocalesPropertyName, supportedLocalesName, KModifier.PRIVATE)
            .initializer(
                CodeBlock
                    .builder()
                    .apply {
                        add("%T(listOf(\n", supportedLocalesName)
                        languages.filter { it != "base" }.forEach { language ->
                            val fileName = getFileNameForLanguage(language)
                            add(
                                "%T(%S, js(%S) as %T),\n",
                                supportedLocaleName,
                                language,
                                "require(\"$folder/$fileName\")",
                                String::class
                            )
                        }
                        add("))")
                    }
                    .build()
            )
            .build()
    )

    addSuperinterface(loaderName)

    addProperty(
        PropertySpec.builder(
            "stringsLoader",
            localizedStringLoaderName,
            KModifier.OVERRIDE
        ).initializer(
            CodeBlock.of(
                "LocalizedStringLoader(supportedLocales = %N, fallbackFileUri = %N)",
                supportedLocalesPropertyName,
                fallbackFilePropertyName
            )
        ).build()
    )
}
