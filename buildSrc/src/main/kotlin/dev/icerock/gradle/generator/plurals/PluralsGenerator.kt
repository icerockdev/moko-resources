/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.plurals

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.generator.MRGenerator
import org.gradle.api.file.FileTree
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.io.File

abstract class PluralsGenerator(
    protected val sourceSet: KotlinSourceSet,
    private val pluralsFileTree: FileTree
) : MRGenerator.Generator {

    override fun generate(resourcesGenerationDir: File): TypeSpec {
        return TypeSpec.objectBuilder("plurals")
            .addModifiers(*getClassModifiers())
            .build()
    }

    override fun getImports(): List<ClassName> = emptyList()

    protected open fun getClassModifiers(): Array<KModifier> = emptyArray()
}
