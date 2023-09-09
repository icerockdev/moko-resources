/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.tasks

import dev.icerock.gradle.generator.MRGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

open class GenerateMultiplatformResourcesTask : DefaultTask() {

    @get:Internal
    internal lateinit var generator: MRGenerator

    @get:InputFiles
    val inputFiles: Iterable<File>
        get() = generator.generators.flatMap { it.inputFiles }

    @get:OutputDirectory
    val outputDirectory: File
        get() = generator.outputDir.get()

    init {
        group = "moko-resources"
    }

    @TaskAction
    fun generate() {
        generator.generate()
    }
}
