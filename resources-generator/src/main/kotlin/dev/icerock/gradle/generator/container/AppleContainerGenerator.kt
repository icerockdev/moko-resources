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

internal class AppleContainerGenerator(
    private val bundleIdentifier: String,
) : PlatformContainerGenerator {
    override fun getImports(): List<ClassName> {
        return listOf(
            Constants.Apple.nsBundleName,
            Constants.Apple.loadableBundleName
        )
    }

    override fun generateAdditionalFiles(packageName: String): List<FileSpec> {
        val provider = TypeSpec.objectBuilder(Constants.PlatformDetails.providerObjectName)
            .addModifiers(KModifier.INTERNAL)
            .addProperty(
                PropertySpec.builder(
                    Constants.Apple.resourcesBundlePropertyName,
                    Constants.Apple.nsBundleName,
                    KModifier.PRIVATE
                ).delegate(CodeBlock.of("lazy { NSBundle.loadableBundle(%S) }", bundleIdentifier))
                    .build()
            )
            .addProperty(
                PropertySpec.builder(
                    Constants.PlatformDetails.providerDetailsPropertyName,
                    Constants.resourcePlatformDetailsName
                ).delegate(
                    CodeBlock.of(
                        "lazy { %T(%N) }",
                        Constants.resourcePlatformDetailsName,
                        Constants.Apple.resourcesBundlePropertyName
                    )
                ).build()
            )
            .build()

        return listOf(
            FileSpec.builder(packageName, Constants.PlatformDetails.providerObjectName)
                .addImport(
                    Constants.Apple.nsBundleName.packageName,
                    Constants.Apple.nsBundleName.simpleNames
                )
                .addImport(
                    Constants.Apple.loadableBundleName.packageName,
                    Constants.Apple.loadableBundleName.simpleNames
                )
                .addType(provider)
                .build()
        )
    }
}
