/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.configuration

import dev.icerock.gradle.generator.MRGenerator
import dev.icerock.gradle.generator.ResourceGeneratorFeature
import dev.icerock.gradle.generator.jvm.JvmMRGenerator
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget

internal fun configureJvmTargetGenerator(
    target: KotlinTarget,
    settings: MRGenerator.Settings,
    features: List<ResourceGeneratorFeature<out MRGenerator.Generator>>
) {
    JvmMRGenerator(
        project = target.project,
        settings = settings,
        generators = features.map { it.createJvmGenerator() }
    ).apply(project = target.project)
    // TODO fix depends on
//            compilation.defaultSourceSet.ifDependsOn(commonSourceSet) {
}
