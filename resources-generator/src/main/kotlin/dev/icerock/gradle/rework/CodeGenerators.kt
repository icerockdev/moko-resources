/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.rework

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec

internal fun TypeSpec.Builder.addAppleResourcesBundleProperty(bundleIdentifier: String) {
    val bundleProperty: PropertySpec = PropertySpec.builder(
        CodeConst.Apple.resourcesBundlePropertyName,
        CodeConst.Apple.nsBundleName,
        KModifier.PRIVATE
    ).delegate(CodeBlock.of("lazy { NSBundle.loadableBundle(%S) }", bundleIdentifier))
        .build()

    addProperty(bundleProperty)
}

internal fun TypeSpec.Builder.addContentHashProperty(hash: String) {
    val bundleProperty: PropertySpec =
        PropertySpec.builder("contentHash", STRING, KModifier.PRIVATE)
            .initializer("%S", hash)
            .build()

    addProperty(bundleProperty)
}

internal fun TypeSpec.Builder.addAppleContainerBundleProperty() {
    val bundleProperty: PropertySpec =
        PropertySpec.builder(
            CodeConst.Apple.containerBundlePropertyName,
            CodeConst.Apple.nsBundleName,
            KModifier.OVERRIDE
        ).initializer(CodeConst.Apple.resourcesBundlePropertyName)
            .build()

    addProperty(bundleProperty)
}

internal fun TypeSpec.Builder.addJvmClassLoaderProperty(resourcesClassName: String) {
    val property: PropertySpec = PropertySpec.builder(
        CodeConst.Jvm.resourcesClassLoaderPropertyName,
        CodeConst.Jvm.classLoaderName,
        KModifier.PRIVATE
    ).initializer(CodeBlock.of("$resourcesClassName::class.java.classLoader"))
        .build()

    addProperty(property)
}

internal fun TypeSpec.Builder.addJsFallbackProperty(fallbackFilePath: String) {
    val property: PropertySpec = PropertySpec
        .builder(CodeConst.Js.fallbackFilePropertyName, String::class, KModifier.PRIVATE)
        .initializer(
            CodeBlock.of(
                "js(%S) as %T",
                "require(\"$fallbackFilePath\")",
                String::class
            )
        )
        .build()

    addProperty(property)
}

internal fun TypeSpec.Builder.addJsSupportedLocalesProperty(
    bcpLangToPath: List<Pair<String, String>>
) {
    val property: PropertySpec = PropertySpec
        .builder(
            CodeConst.Js.supportedLocalesPropertyName,
            CodeConst.Js.supportedLocalesName,
            KModifier.PRIVATE
        ).initializer(
            CodeBlock
                .builder()
                .apply {
                    add("%T(listOf(\n", CodeConst.Js.supportedLocalesName)
                    bcpLangToPath.forEach { (bcpLang, filePath) ->
                        add(
                            "%T(%S, js(%S) as %T),\n",
                            CodeConst.Js.supportedLocaleName,
                            bcpLang,
                            "require(\"$filePath\")",
                            String::class
                        )
                    }
                    add("))")
                }.build()
        ).build()

    addProperty(property)
}

internal fun TypeSpec.Builder.addJsContainerStringsLoaderProperty() {
    val property = PropertySpec.builder(
        CodeConst.Js.stringsLoaderPropertyName,
        CodeConst.Js.stringLoaderName,
        KModifier.OVERRIDE
    ).initializer(
        CodeBlock.of(
            "RemoteJsStringLoader.Impl(supportedLocales = %N, fallbackFileUri = %N)",
            CodeConst.Js.supportedLocalesPropertyName,
            CodeConst.Js.fallbackFilePropertyName
        )
    ).build()
    addProperty(property)
}
