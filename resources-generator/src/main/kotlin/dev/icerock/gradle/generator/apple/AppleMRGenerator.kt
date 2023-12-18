/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.apple

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.generator.TargetMRGenerator
import dev.icerock.gradle.utils.calculateResourcesHash
import org.gradle.api.Project
import java.io.File

@Suppress("TooManyFunctions")
class AppleMRGenerator(
    project: Project,
    settings: Settings,
    generators: List<Generator>,
) : TargetMRGenerator(
    project = project,
    settings = settings,
    generators = generators
) {
    override val resourcesGenerationDir: File = settings.resourcesDir.asFile

    private val bundleClassName = ClassName("platform.Foundation", "NSBundle")
    private val bundleIdentifier = "${settings.packageName}.MR"

    override fun processMRClass(mrClass: TypeSpec.Builder) {
        super.processMRClass(mrClass)

        mrClass.addProperty(
            PropertySpec.builder(
                BUNDLE_PROPERTY_NAME,
                bundleClassName,
                KModifier.PRIVATE
            )
                .delegate(CodeBlock.of("lazy { NSBundle.loadableBundle(\"${bundleIdentifier}\") }"))
                .build()
        )

        mrClass.addProperty(
            PropertySpec.builder("contentHash", STRING, KModifier.PRIVATE)
                .initializer("%S", resourcesGenerationDir.calculateResourcesHash())
                .build()
        )
    }

    override fun getImports(): List<ClassName> = listOf(
        bundleClassName,
        ClassName("dev.icerock.moko.resources.utils", "loadableBundle")
    )

    override fun beforeMRGeneration() {
        assetsGenerationDir.mkdirs()
    }

    companion object {
        const val BUNDLE_PROPERTY_NAME = "bundle"
        const val ASSETS_DIR_NAME = "Assets.xcassets"
    }
}
