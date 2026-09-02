/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.container

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.generator.Constants
import dev.icerock.gradle.generator.PlatformContainerGenerator

internal class JvmContainerGenerator : PlatformContainerGenerator {
    override fun getImports(): List<ClassName> {
        return listOf(
            Constants.Jvm.classLoaderName,
            ClassName("kotlin.jvm", "java")
        )
    }

    override fun generateAdditionalFiles(packageName: String): List<FileSpec> {
        val provider = TypeSpec.objectBuilder(Constants.PlatformDetails.providerObjectName)
            .addModifiers(KModifier.INTERNAL)
            .addProperty(
                PropertySpec.builder(
                    Constants.Jvm.resourcesClassLoaderPropertyName,
                    Constants.Jvm.classLoaderName,
                    KModifier.PRIVATE
                ).initializer(
                    CodeBlock.of(
                        "%L::class.java.classLoader",
                        Constants.PlatformDetails.providerObjectName
                    )
                ).build()
            )
            .addProperty(
                PropertySpec.builder(
                    Constants.PlatformDetails.providerDetailsPropertyName,
                    Constants.resourcePlatformDetailsName
                ).delegate(
                    CodeBlock.of(
                        "lazy { %T(%N) }",
                        Constants.resourcePlatformDetailsName,
                        Constants.Jvm.resourcesClassLoaderPropertyName
                    )
                ).build()
            )
            .build()

        return listOf(
            FileSpec.builder(packageName, Constants.PlatformDetails.providerObjectName)
                .addImport("kotlin.jvm", "java")
                .addType(provider)
                .build()
        )
    }
}
