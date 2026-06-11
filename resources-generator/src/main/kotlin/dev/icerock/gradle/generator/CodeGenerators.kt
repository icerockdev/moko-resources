/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("TooManyFunctions")

package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.generator.Constants.Apple
import dev.icerock.gradle.generator.Constants.Jvm
import dev.icerock.gradle.generator.Constants.PlatformDetails
import dev.icerock.gradle.generator.platform.js.JsFilePathMode
import dev.icerock.gradle.metadata.resource.HierarchyMetadata
import dev.icerock.gradle.metadata.resource.ResourceMetadata
import org.gradle.api.GradleException
import kotlin.DeprecationLevel

internal fun TypeSpec.Builder.addAppleResourcesBundleProperty(bundleIdentifier: String) {
    val bundleProperty: PropertySpec = PropertySpec.builder(
        Apple.resourcesBundlePropertyName,
        Apple.nsBundleName,
        KModifier.PRIVATE
    ).delegate(CodeBlock.of("lazy { NSBundle.loadableBundle(%S) }", bundleIdentifier))
        .build()

    addProperty(bundleProperty)
}

internal fun FileSpec.Builder.addAppleResourcesBundleProperty(bundleIdentifier: String) {
    addProperty(
        PropertySpec.builder(
            Apple.resourcesBundlePropertyName,
            Apple.nsBundleName,
            KModifier.PRIVATE
        ).delegate(CodeBlock.of("lazy { NSBundle.loadableBundle(%S) }", bundleIdentifier))
            .build()
    )
}

internal fun TypeSpec.Builder.addContentHashProperty(hash: String) {
    val bundleProperty: PropertySpec =
        PropertySpec.builder("contentHash", STRING, KModifier.PRIVATE)
            .initializer("%S", hash)
            .build()

    addProperty(bundleProperty)
}

internal fun TypeSpec.Builder.addAppleContainerBundleInitializerProperty(
    modifier: KModifier? = null,
) {
    addContainerPlatformDetailsProperty(
        initializer = CodeBlock.of(PlatformDetails.providerReference),
        modifier = modifier
    )
}

internal fun TypeSpec.Builder.addAppleBatchedBundleInitializerProperty() {
    addBatchedPlatformDetailsProperty(
        initializer = CodeBlock.of(
            "${PlatformDetails.platformDetailsClass}(${Apple.resourcesBundlePropertyName})"
        )
    )
}

internal fun FileSpec.Builder.addAppleBatchedBundleInitializerProperty() {
    addBatchedPlatformDetailsProperty(
        initializer = CodeBlock.of(
            "${PlatformDetails.platformDetailsClass}(${Apple.resourcesBundlePropertyName})"
        )
    )
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

internal fun FileSpec.Builder.addJvmClassLoaderProperty(resourcesClassName: String) {
    addImport("kotlin.jvm", "java")
    addProperty(
        PropertySpec.builder(
            Jvm.resourcesClassLoaderPropertyName,
            Jvm.classLoaderName,
            KModifier.PRIVATE
        ).initializer(CodeBlock.of("$resourcesClassName::class.java.classLoader"))
            .build()
    )
}

internal fun TypeSpec.Builder.addJvmPlatformResourceClassLoaderProperty(
    modifier: KModifier? = null,
) {
    addContainerPlatformDetailsProperty(
        initializer = CodeBlock.of(PlatformDetails.providerReference),
        modifier = modifier
    )
}

internal fun TypeSpec.Builder.addJvmBatchedPlatformResourceClassLoaderProperty() {
    addBatchedPlatformDetailsProperty(
        initializer = CodeBlock.of(
            "${PlatformDetails.platformDetailsClass}(${Jvm.resourcesClassLoaderPropertyName})"
        )
    )
}

internal fun FileSpec.Builder.addJvmBatchedPlatformResourceClassLoaderProperty() {
    addBatchedPlatformDetailsProperty(
        initializer = CodeBlock.of(
            "${PlatformDetails.platformDetailsClass}(${Jvm.resourcesClassLoaderPropertyName})"
        )
    )
}

internal fun TypeSpec.Builder.addJvmPlatformResourceBundleProperty(
    bundlePropertyName: String,
    bundlePath: String
) {
    val property: PropertySpec = PropertySpec.builder(
        name = bundlePropertyName,
        type = STRING,
        KModifier.PRIVATE
    ).initializer(CodeBlock.of("\"%L/%L\"", Jvm.localizationDir, bundlePath))
        .build()

    addProperty(property)
}

internal fun FileSpec.Builder.addJvmPlatformResourceBundleProperty(
    bundlePropertyName: String,
    bundlePath: String
) {
    addProperty(
        PropertySpec.builder(
            name = bundlePropertyName,
            type = STRING,
            KModifier.PRIVATE
        ).initializer(CodeBlock.of("\"%L/%L\"", Jvm.localizationDir, bundlePath))
            .build()
    )
}

internal fun TypeSpec.Builder.addEmptyPlatformResourceProperty(
    modifier: KModifier? = null,
) {
    addContainerPlatformDetailsProperty(
        initializer = CodeBlock.of("${PlatformDetails.platformDetailsClass}()"),
        modifier = modifier
    )
}

internal fun TypeSpec.Builder.addEmptyBatchedPlatformResourceProperty() {
    addBatchedPlatformDetailsProperty(
        initializer = CodeBlock.of("${PlatformDetails.platformDetailsClass}()")
    )
}

internal fun FileSpec.Builder.addEmptyBatchedPlatformResourceProperty() {
    addBatchedPlatformDetailsProperty(
        initializer = CodeBlock.of("${PlatformDetails.platformDetailsClass}()")
    )
}

internal fun <T : ResourceMetadata> TypeSpec.Builder.addValuesFunction(
    metadata: List<T>,
    classType: ClassName,
    modifier: KModifier? = null,
    memberModifier: KModifier? = null,
    isExpect: Boolean = false,
) {
    addResourceValuesFunction(
        metadata = metadata,
        classType = classType,
        modifier = modifier,
        memberModifier = memberModifier,
        isOverride = true,
        isExpect = isExpect
    )
}

internal fun <T : ResourceMetadata> TypeSpec.Builder.addPlainValuesFunction(
    metadata: List<T>,
    classType: ClassName,
    modifier: KModifier? = null,
    memberModifier: KModifier? = null,
    isExpect: Boolean = false,
) {
    addResourceValuesFunction(
        metadata = metadata,
        classType = classType,
        modifier = modifier,
        memberModifier = memberModifier,
        isOverride = false,
        isExpect = isExpect
    )
}

internal fun TypeSpec.Builder.addAbstractValuesFunction(
    classType: ClassName,
): TypeSpec.Builder {
    val valuesFun: FunSpec = FunSpec.builder("values")
        .returns(
            ClassName(packageName = "kotlin.collections", "List")
                .parameterizedBy(classType)
        )
        .build()

    return addFunction(valuesFun)
}

private fun <T : ResourceMetadata> TypeSpec.Builder.addResourceValuesFunction(
    metadata: List<T>,
    classType: ClassName,
    modifier: KModifier?,
    memberModifier: KModifier?,
    isOverride: Boolean,
    isExpect: Boolean = false,
) {
    // Find metadata type
    val resourceMetadata: T = metadata.first()
    val languageKeysList: String =
        if (resourceMetadata is HierarchyMetadata) {
            // For Assets and Files need create key considering File path
            val hierarchyMetadata: List<HierarchyMetadata> = metadata
                .filterIsInstance<HierarchyMetadata>()
                .takeIf {
                    it.size == metadata.size
                } ?: throw GradleException("Invalid ResourceMetadata type for Assets or Files")

            hierarchyMetadata.joinToString { meta ->
                meta.path.joinToString(separator = ".") +
                    (".".takeIf { meta.path.isNotEmpty() } ?: "") +
                    meta.key
            }
        } else {
            // Create simple resource key
            metadata.joinToString { it.key }
        }

    val valuesFun: FunSpec = FunSpec.builder("values")
        .also {
            if (memberModifier != null) {
                it.addModifiers(memberModifier)
            }
            if (modifier != null) {
                it.addModifiers(modifier)
            }
            if (isOverride) {
                it.addModifiers(KModifier.OVERRIDE)
            }
        }
        .apply {
            if (!isExpect) {
                addStatement("return listOf($languageKeysList)")
            }
        }
        .returns(
            ClassName(packageName = "kotlin.collections", "List")
                .parameterizedBy(classType)
        )
        .build()

    addFunction(valuesFun)
}

internal fun TypeSpec.Builder.addBatchValuesFunction(
    groupNames: List<String>,
    classType: ClassName,
    modifier: KModifier? = null,
) {
    val valuesFun: FunSpec = FunSpec.builder("values")
        .also {
            if (modifier != null) {
                it.addModifiers(modifier)
            }
        }
        .addModifiers(KModifier.OVERRIDE)
        .addCode(
            CodeBlock.builder()
                .apply {
                    if (groupNames.isEmpty()) {
                        addStatement("return emptyList()")
                    } else if (groupNames.size == 1) {
                        addStatement("return %N.values()", groupNames.single())
                    } else {
                        add("return listOf(\n")
                        groupNames.forEach { groupName ->
                            add("%N.values(),\n", groupName)
                        }
                        add(").flatten()\n")
                    }
                }
                .build()
        )
        .returns(
            ClassName(packageName = "kotlin.collections", "List")
                .parameterizedBy(classType)
        )
        .build()

    addFunction(valuesFun)
}

internal fun TypeSpec.Builder.addReferencedValuesFunction(
    propertyReferences: List<String>,
    classType: ClassName,
    memberModifier: KModifier? = null,
) {
    val valuesFun: FunSpec = FunSpec.builder("values")
        .also {
            if (memberModifier != null) {
                it.addModifiers(memberModifier)
            }
        }
        .addStatement(
            "return listOf(%L)",
            propertyReferences.joinToString()
        )
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
    )
        .addAnnotation(hiddenInternalApiAnnotation())
        .build()

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

private fun hiddenInternalApiAnnotation(): AnnotationSpec {
    return AnnotationSpec.builder(Deprecated::class)
        .addMember("message = %S", "Internal resource container detail")
        .addMember("level = %T.HIDDEN", DeprecationLevel::class)
        .build()
}

internal fun TypeSpec.Builder.addJsFallbackProperty(
    fallbackFilePath: String,
    filePathMode: JsFilePathMode
) {
    val property: PropertySpec = PropertySpec
        .builder(Constants.Js.fallbackFilePropertyName, String::class, KModifier.PRIVATE)
        .initializer(
            CodeBlock.of(
                "${filePathMode.format} as %T",
                filePathMode.argument(fallbackFilePath),
                String::class
            )
        )
        .build()

    addProperty(property)
}

internal fun FileSpec.Builder.addJsFallbackProperty(
    fallbackFilePath: String,
    filePathMode: JsFilePathMode
) {
    addProperty(
        PropertySpec
            .builder(Constants.Js.fallbackFilePropertyName, String::class, KModifier.PRIVATE)
            .initializer(
                CodeBlock.of(
                    "${filePathMode.format} as %T",
                    filePathMode.argument(fallbackFilePath),
                    String::class
                )
            )
            .build()
    )
}

internal fun TypeSpec.Builder.addJsSupportedLocalesProperty(
    bcpLangToPath: List<Pair<String, String>>,
    filePathMode: JsFilePathMode
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
                            "%T(%S, ${filePathMode.format} as %T),\n",
                            Constants.Js.supportedLocaleName,
                            bcpLang,
                            filePathMode.argument(filePath),
                            String::class
                        )
                    }
                    add("))")
                }.build()
        ).build()

    addProperty(property)
}

internal fun FileSpec.Builder.addJsSupportedLocalesProperty(
    bcpLangToPath: List<Pair<String, String>>,
    filePathMode: JsFilePathMode
) {
    addProperty(
        PropertySpec
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
                                "%T(%S, ${filePathMode.format} as %T),\n",
                                Constants.Js.supportedLocaleName,
                                bcpLang,
                                filePathMode.argument(filePath),
                                String::class
                            )
                        }
                        add("))")
                    }.build()
            ).build()
    )
}

internal fun TypeSpec.Builder.addJsContainerStringsLoaderProperty() {
    val property = PropertySpec.builder(
        Constants.Js.stringsLoaderPropertyName,
        Constants.Js.stringLoaderName,
        KModifier.OVERRIDE
    ).initializer(
        CodeBlock.of(
            "${Constants.Js.remoteStringLoaderClassName}.Impl(supportedLocales = %N, fallbackFileUri = %N)",
            Constants.Js.supportedLocalesPropertyName,
            Constants.Js.fallbackFilePropertyName
        )
    ).build()
    addProperty(property)
}

internal fun TypeSpec.Builder.addJsBatchedStringsLoaderProperty() {
    val property = PropertySpec.builder(
        Constants.Js.stringsLoaderPropertyName,
        Constants.Js.stringLoaderName,
        KModifier.PRIVATE
    ).initializer(
        CodeBlock.of(
            "${Constants.Js.remoteStringLoaderClassName}.Impl(supportedLocales = %N, fallbackFileUri = %N)",
            Constants.Js.supportedLocalesPropertyName,
            Constants.Js.fallbackFilePropertyName
        )
    ).build()
    addProperty(property)
}

internal fun FileSpec.Builder.addJsBatchedStringsLoaderProperty() {
    addProperty(
        PropertySpec.builder(
            Constants.Js.stringsLoaderPropertyName,
            Constants.Js.stringLoaderName,
            KModifier.PRIVATE
        ).initializer(
            CodeBlock.of(
                "${Constants.Js.remoteStringLoaderClassName}.Impl(supportedLocales = %N, fallbackFileUri = %N)",
                Constants.Js.supportedLocalesPropertyName,
                Constants.Js.fallbackFilePropertyName
            )
        ).build()
    )
}

private fun TypeSpec.Builder.addContainerPlatformDetailsProperty(
    initializer: CodeBlock,
    modifier: KModifier? = null,
) {
    val resourcePlatformDetailsPropertySpec = PropertySpec
        .builder(
            PlatformDetails.platformDetailsPropertyName,
            Constants.resourcePlatformDetailsName
        )
        .addAnnotation(hiddenInternalApiAnnotation())
        .also {
            if (modifier != null) {
                it.addModifiers(modifier)
            }
        }
        .addModifiers(KModifier.OVERRIDE)
        .initializer(initializer)
        .build()

    addProperty(resourcePlatformDetailsPropertySpec)
}

private fun TypeSpec.Builder.addBatchedPlatformDetailsProperty(
    initializer: CodeBlock,
) {
    val resourcePlatformDetailsPropertySpec = PropertySpec
        .builder(
            PlatformDetails.platformDetailsPropertyName,
            Constants.resourcePlatformDetailsName,
            KModifier.PRIVATE
        )
        .initializer(initializer)
        .build()

    addProperty(resourcePlatformDetailsPropertySpec)
}

private fun FileSpec.Builder.addBatchedPlatformDetailsProperty(
    initializer: CodeBlock,
) {
    addProperty(
        PropertySpec
            .builder(
                PlatformDetails.platformDetailsPropertyName,
                Constants.resourcePlatformDetailsName,
                KModifier.PRIVATE
            )
            .initializer(initializer)
            .build()
    )
}
