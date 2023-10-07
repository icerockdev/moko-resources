/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

@CacheableTask
abstract class GenerateMultiplatformResourcesTask : DefaultTask() {

    @get:InputFiles
    @get:Classpath
    abstract val ownResources: ConfigurableFileCollection

    @get:InputFiles
    @get:Classpath
    abstract val lowerResources: ConfigurableFileCollection

    @get:InputFiles
    @get:Classpath
    abstract val upperResources: ConfigurableFileCollection

    @get:Input
    abstract val kotlinTarget: Property<String>

//    @get:Input
//    val kotlinSourceSet: Property<KotlinSourceSet> = objectFactory.property()

//    @get:InputFiles
//    val inputFiles: Iterable<File>
//        get() = generator.generators.flatMap { it.inputFiles }
//
//    @get:OutputDirectory
//    val outputDirectory: File
//        get() = generator.outputDir.get()

    init {
        group = "moko-resources"
        kotlinTarget.convention("kotlinMetadata")
    }

    @TaskAction
    fun generate() {
        logger.warn("i $name have ownResources ${ownResources.from}")
        logger.warn("i $name have lowerResources ${lowerResources.from}")
        logger.warn("i $name have upperResources ${upperResources.from}")
        logger.warn("i $name have kotlinTarget ${kotlinTarget.get()}")

//        val kmpExtension: KotlinMultiplatformExtension = project.extensions.getByType()
//        val sourceSet: KotlinSourceSet = kotlinSourceSet.get()
//        val ownSourceDirectory: SourceDirectorySet =
//            sourceSet.extras[mokoResourcesSourceDirectoryKey()]
//                ?: error("can't find source directory!")
//
//        val allSourceDirectories = sourceSet.allSourceSets()
//            .map { it.extras[mokoResourcesSourceDirectoryKey()] }
//            .onEach { project.logger.warn("found mrs $it") }
//            .filterNotNull()
//
//        val (target, compilation) = kmpExtension.targets
//            .onEach { project.logger.warn("found target $it") }
//            .flatMap { target ->
//                target.compilations.map { target to it }
//            }
//            .onEach { (target, compilation) ->
//                project.logger.warn("found $target $compilation with ${compilation.defaultSourceSet} ${compilation.kotlinSourceSets}")
//            }
//            .filter { (target, compilation) ->
//                compilation.defaultSourceSet == sourceSet
//            }
//            .onEach { project.logger.warn("found with default $it") }
//            .first()
//
//        project.logger.warn("${this.name} from $compilation ${compilation?.target}")
//
//        project.logger.warn("ownSourceDirectory $ownSourceDirectory")
//        project.logger.warn("allSourceDirectories $allSourceDirectories")

        // TODO when task executed we should detect which generator should be used by check source
        //  sets hierarhy

    }

    private fun KotlinSourceSet.allSourceSets(): Set<KotlinSourceSet> {
        return setOf(this) + dependsOn.flatMap { it.allSourceSets() }
    }
}
