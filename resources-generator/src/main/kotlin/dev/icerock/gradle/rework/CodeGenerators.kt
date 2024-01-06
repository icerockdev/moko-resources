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
        CodeConst.Apple.nsBundleClass,
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
            CodeConst.Apple.nsBundleClass,
            KModifier.OVERRIDE
        ).initializer(CodeConst.Apple.resourcesBundlePropertyName)
            .build()

    addProperty(bundleProperty)
}

internal fun TypeSpec.Builder.addJvmClassLoaderProperty(resourcesClassName: String) {
    val property: PropertySpec = PropertySpec.builder(
        CodeConst.Jvm.resourcesClassLoaderPropertyName,
        CodeConst.Jvm.classLoaderClass,
        KModifier.PRIVATE
    ).initializer(CodeBlock.of("$resourcesClassName::class.java.classLoader"))
        .build()

    addProperty(property)
}
