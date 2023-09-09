/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.configuration

import dev.icerock.gradle.generator.MRGenerator
import dev.icerock.gradle.generator.ResourceGeneratorFeature
import dev.icerock.gradle.generator.js.JsMRGenerator
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrTarget

internal fun configureJsTargetGenerator(
    target: KotlinTarget,
    settings: MRGenerator.Settings,
    features: List<ResourceGeneratorFeature<out MRGenerator.Generator>>
) {
    val project: Project = target.project
    val jsTarget: KotlinJsIrTarget? = target as? KotlinJsIrTarget
    if (jsTarget == null) {
        project.logger.warn("$target is not supported by MOKO Resources")
        return
    }

    jsTarget.compilations.configureEach { compilation ->
        // TODO rollback ifDepends
//            compilation.defaultSourceSet.ifDependsOn(commonSourceSet) {
        JsMRGenerator(
            generatedDir = settings.generatedDir,
            sourceSet = project.provider { createSourceSet(compilation.defaultSourceSet) },
            settings = settings,
            generators = features.map { it.createJsGenerator() },
            compilation = compilation,
        ).apply(project = project)
//            }
    }
}
