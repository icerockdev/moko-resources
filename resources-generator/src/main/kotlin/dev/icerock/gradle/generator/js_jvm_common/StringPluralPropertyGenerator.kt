/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.js_jvm_common

import com.squareup.kotlinpoet.*
import dev.icerock.gradle.generator.LanguageType

/**
 * @param languages the language codes supported
 * @param fallbackFilePropertyName the property name in the MR object
 * @param fallbackFile the name of the file webpack will use, e.g. my_plurals.json
 */
fun TypeSpec.Builder.generateFallbackAndSupportedLanguageProperties(
    languages: List<LanguageType>,
    fallbackFilePropertyName: String,
    fallbackFile: String,
    supportedLocalesPropertyName: String,
    getFileNameForLanguage: (language: String) -> String
) {
    addProperty(
        PropertySpec
            .builder(fallbackFilePropertyName, String::class, KModifier.PRIVATE)
            .initializer(
                CodeBlock.of(
                    "js(%S) as %T",
                    "require(\"$fallbackFile\")",
                    String::class
                )
            )
            .build()
    )

    val supportedLocales = ClassName("dev.icerock.moko.resources", "SupportedLocales")
    val supportedLocale = ClassName("dev.icerock.moko.resources", "SupportedLocale")
    addProperty(
        PropertySpec
            .builder(supportedLocalesPropertyName, supportedLocales, KModifier.PRIVATE)
            .initializer(
                CodeBlock
                    .builder()
                    .apply {
                        add("%T(listOf(\n", supportedLocales)
                        languages.filter { it != "base" }.forEach { language ->
                            val fileName = getFileNameForLanguage(language)
                            add(
                                "%T(%S, js(%S) as %T),\n",
                                supportedLocale,
                                language,
                                "require(\"$fileName\")",
                                String::class
                            )
                        }
                        add("))")
                    }
                    .build()
            )
            .build()
    )
}