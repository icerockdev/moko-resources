/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.js

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.generator.MRGenerator
import org.gradle.api.Project
import org.gradle.api.Task
import java.io.File

class JsMRGenerator(
    generatedDir: File,
    sourceSet: SourceSet,
    mrClassPackage: String,
    generators: List<Generator>
) : MRGenerator(
    generatedDir = generatedDir,
    sourceSet = sourceSet,
    mrClassPackage = mrClassPackage,
    generators = generators
) {

    override val sourcesGenerationDir: File
        get() = super.sourcesGenerationDir
    override val resourcesGenerationDir: File
        get() = super.resourcesGenerationDir

    override fun beforeMRGeneration() {
        super.beforeMRGeneration()
    }

    override fun afterMRGeneration() {
        super.afterMRGeneration()
    }

    override fun getMRClassModifiers(): Array<KModifier> {
        TODO("Not yet implemented")
    }

    override fun apply(generationTask: Task, project: Project) {
        TODO("Not yet implemented")
    }

    override fun processMRClass(mrClass: TypeSpec.Builder) {
        super.processMRClass(mrClass)
    }

    override fun getImports(): List<ClassName> {
        return super.getImports()
    }
}