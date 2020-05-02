/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.KModifier
import org.gradle.api.Project
import org.gradle.api.Task
import java.io.File

class CommonMRGenerator(
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
    override fun getMRClassModifiers(): Array<KModifier> = arrayOf(KModifier.EXPECT)

    override fun apply(generationTask: Task, project: Project) {}
}
