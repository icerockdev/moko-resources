/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.generator.Constants.Apple
import dev.icerock.gradle.generator.Constants.Jvm
import dev.icerock.gradle.generator.Constants.PlatformDetails
import dev.icerock.gradle.metadata.resource.ResourceMetadata

internal fun TypeSpec.Builder.addAppleResourcesBundleProperty(bundleIdentifier: String) {
    val bundleProperty: PropertySpec = PropertySpec.builder(
        Apple.resourcesBundlePropertyName,
        Apple.nsBundleName,
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

internal fun TypeSpec.Builder.addAppleContainerBundleInitializerProperty(
    modifiers: List<KModifier>
) {
    val codeBlock = "${PlatformDetails.platformDetailsClass}(${Apple.resourcesBundlePropertyName})"

    val resourcePlatformDetailsPropertySpec = PropertySpec
        .builder(
            PlatformDetails.platformDetailsPropertyName,
            Constants.resourcePlatformDetailsName
        )
        .addModifiers(modifiers)
        .addModifiers(KModifier.OVERRIDE)
        .initializer(
            CodeBlock.of(codeBlock)
        ).build()

    addProperty(resourcePlatformDetailsPropertySpec)
}

internal fun TypeSpec.Builder.addJvmClassLoaderProperty(resourcesClassName: String) {
    val property: PropertySpec = PropertySpec.builder(
        Jvm.resourcesClassLoaderPropertyName,
        Jvm.classLoaderName,
        KModifier.PRIVATE
    ).initializer(CodeBlock.of("$resourcesClassName::class.java.classLoader"))
        .build()

    addProperty(property)
}

internal fun TypeSpec.Builder.addJvmPlatformResourceClassLoaderProperty(
    modifiers: List<KModifier>,
    resourcesClassName: String
) {
    val codeInitProperty: String = resourcesClassName + "." + Jvm.resourcesClassLoaderPropertyName
    val codeBlock = "${PlatformDetails.platformDetailsClass}($codeInitProperty)"

    val resourcePlatformDetailsPropertySpec = PropertySpec
        .builder(
            PlatformDetails.platformDetailsPropertyName,
            Constants.resourcePlatformDetailsName
        )
        .addModifiers(modifiers)
        .addModifiers(KModifier.OVERRIDE)
        .initializer(CodeBlock.of(codeBlock))
        .build()

    addProperty(resourcePlatformDetailsPropertySpec)
}

internal fun TypeSpec.Builder.addEmptyPlatformResourceProperty(
    modifiers: List<KModifier>
) {
    val resourcePlatformDetailsPropertySpec = PropertySpec
        .builder(
            PlatformDetails.platformDetailsPropertyName,
            Constants.resourcePlatformDetailsName
        )
        .addModifiers(modifiers)
        .addModifiers(KModifier.OVERRIDE)
        .initializer(
            CodeBlock.of("${PlatformDetails.platformDetailsClass}()")
        ).build()

    addProperty(resourcePlatformDetailsPropertySpec)
}

internal fun TypeSpec.Builder.addValuesFunction(
    modifiers: List<KModifier>,
    metadata: List<ResourceMetadata>,
    classType: ClassName,
) {
    val languageKeysList: String = metadata.joinToString { it.key }

    val valuesFun: FunSpec = FunSpec.builder("values")
        .addModifiers(modifiers)
        .addModifiers(KModifier.OVERRIDE)
        .addStatement("return listOf($languageKeysList)")
        .returns(
            ClassName(packageName = "kotlin.collections", "List")
                .parameterizedBy(classType)
        )
        .build()

    addFunction(valuesFun)
}

internal fun TypeSpec.Builder.addOverridePlatformProperty(): TypeSpec.Builder {
    val resourcePlatformDetailsPropertySpec = PropertySpec.builder(
        PlatformDetails.platformDetailsPropertyName,
        Constants.resourcePlatformDetailsName,
        KModifier.OVERRIDE
    ).build()

    return addProperty(resourcePlatformDetailsPropertySpec)
}

internal fun TypeSpec.Builder.addOverrideAbstractValuesFunction(
    classType: ClassName,
): TypeSpec.Builder {
    val valuesFun: FunSpec = FunSpec.builder("values")
        .addModifiers(KModifier.OVERRIDE)
        .returns(
            ClassName("kotlin.collections", "List")
                .parameterizedBy(classType)
        )
        .build()

    return addFunction(valuesFun)
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
    bcpLangToPath: List<Pair<String, String>>,
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
