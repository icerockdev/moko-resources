/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec

internal fun TypeSpec.Builder.addAppleResourcesBundleProperty(bundleIdentifier: String) {
    val bundleProperty: PropertySpec = PropertySpec.builder(
        Constants.Apple.resourcesBundlePropertyName,
        Constants.Apple.nsBundleName,
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
            Constants.Apple.containerBundlePropertyName,
            Constants.Apple.nsBundleName,
            KModifier.OVERRIDE
        ).initializer(Constants.Apple.resourcesBundlePropertyName)
            .build()

    addProperty(bundleProperty)
}

internal fun TypeSpec.Builder.addJvmClassLoaderProperty(resourcesClassName: String) {
    val property: PropertySpec = PropertySpec.builder(
        Constants.Jvm.resourcesClassLoaderPropertyName,
        Constants.Jvm.classLoaderName,
        KModifier.PRIVATE
    ).initializer(CodeBlock.of("$resourcesClassName::class.java.classLoader"))
        .build()

    addProperty(property)
}

internal fun TypeSpec.Builder.addJvmResourcesClassLoaderProperty(resourcesClassName: String) {
    val classLoaderProperty: PropertySpec = PropertySpec.builder(
        Constants.Jvm.resourcesClassLoaderPropertyName,
        Constants.Jvm.classLoaderName,
        KModifier.OVERRIDE
    )
        .initializer(CodeBlock.of(resourcesClassName + "." + Constants.Jvm.resourcesClassLoaderPropertyName))
        .build()

    addProperty(classLoaderProperty)
}

internal fun TypeSpec.Builder.addJsFallbackProperty(fallbackFilePath: String) {
    val property: PropertySpec = PropertySpec
        .builder(Constants.Js.fallbackFilePropertyName, String::class, KModifier.PRIVATE)
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
            Constants.Js.supportedLocalesPropertyName,
            Constants.Js.supportedLocalesName,
            KModifier.PRIVATE
        ).initializer(
            CodeBlock
                .builder()
                .apply {
                    add("%T(listOf(\n", Constants.Js.supportedLocalesName)
                    bcpLangToPath.forEach { (bcpLang, filePath) ->
                        add(
                            "%T(%S, js(%S) as %T),\n",
                            Constants.Js.supportedLocaleName,
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
        Constants.Js.stringsLoaderPropertyName,
        Constants.Js.stringLoaderName,
        KModifier.OVERRIDE
    ).initializer(
        CodeBlock.of(
            "RemoteJsStringLoader.Impl(supportedLocales = %N, fallbackFileUri = %N)",
            Constants.Js.supportedLocalesPropertyName,
            Constants.Js.fallbackFilePropertyName
        )
    ).build()
    addProperty(property)
}
