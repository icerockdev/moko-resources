/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.configuration

import dev.icerock.gradle.generator.MRGenerator
import dev.icerock.gradle.generator.ResourceGeneratorFeature
import dev.icerock.gradle.generator.common.CommonMRGenerator
import dev.icerock.gradle.tasks.GenerateMultiplatformResourcesTask
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget

internal fun configureCommonTargetGenerator(
    target: KotlinTarget,
    settings: MRGenerator.Settings,
    features: List<ResourceGeneratorFeature<out MRGenerator.Generator>>
) {
    val generationTask: GenerateMultiplatformResourcesTask = CommonMRGenerator(
        generatedDir = settings.generatedDir,
        sourceSet = target.project.provider {
            val compilation = target.compilations.getByName(KotlinCompilation.MAIN_COMPILATION_NAME)
            createSourceSet(compilation.defaultSourceSet)
        },
        settings = settings,
        generators = features.map { it.createCommonGenerator() }
    ).apply(project = target.project)

    target.project.tasks
        .withType<GenerateMultiplatformResourcesTask>()
        .matching { it != generationTask }
        .configureEach { it.dependsOn(generationTask) }
}
