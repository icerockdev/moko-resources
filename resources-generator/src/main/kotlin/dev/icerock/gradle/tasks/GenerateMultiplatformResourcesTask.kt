/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.tasks

import dev.icerock.gradle.mokoResourcesSourceDirectoryKey
import org.gradle.api.DefaultTask
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.property
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import javax.inject.Inject

@CacheableTask
abstract class GenerateMultiplatformResourcesTask @Inject constructor(
    objectFactory: ObjectFactory,
) : DefaultTask() {

    @get:Input
    val kotlinSourceSet: Property<KotlinSourceSet> = objectFactory.property()

//    @get:InputFiles
//    val inputFiles: Iterable<File>
//        get() = generator.generators.flatMap { it.inputFiles }
//
//    @get:OutputDirectory
//    val outputDirectory: File
//        get() = generator.outputDir.get()

    init {
        group = "moko-resources"
    }

    @TaskAction
    fun generate() {
        val kmpExtension: KotlinMultiplatformExtension = project.extensions.getByType()
        val sourceSet: KotlinSourceSet = kotlinSourceSet.get()
        val ownSourceDirectory: SourceDirectorySet =
            sourceSet.extras[mokoResourcesSourceDirectoryKey()]
                ?: error("can't find source directory!")

        val allSourceDirectories = sourceSet.allSourceSets()
            .map { it.extras[mokoResourcesSourceDirectoryKey()] }
            .onEach { project.logger.warn("found mrs $it") }
            .filterNotNull()

        val (target, compilation) = kmpExtension.targets
            .onEach { project.logger.warn("found target $it") }
            .flatMap { target ->
                target.compilations.map { target to it }
            }
            .onEach { (target, compilation) ->
                project.logger.warn("found $target $compilation with ${compilation.defaultSourceSet} ${compilation.kotlinSourceSets}")
            }
            .filter { (target, compilation) ->
                compilation.defaultSourceSet == sourceSet
            }
            .onEach { project.logger.warn("found with default $it") }
            .first()

        project.logger.warn("${this.name} from $compilation ${compilation?.target}")

        project.logger.warn("ownSourceDirectory $ownSourceDirectory")
        project.logger.warn("allSourceDirectories $allSourceDirectories")
    }

    private fun KotlinSourceSet.allSourceSets(): Set<KotlinSourceSet> {
        return setOf(this) + dependsOn.flatMap { it.allSourceSets() }
    }
}
