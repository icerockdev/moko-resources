/*
 * Copyright 2025 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.platform.android

import com.android.build.api.dsl.AndroidSourceSet
import com.android.build.api.variant.Variant
import dev.icerock.gradle.tasks.GenerateMultiplatformResourcesTask
import org.gradle.api.GradleException
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.extra
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJvmAndroidCompilation
import org.jetbrains.kotlin.gradle.plugin.sources.android.findAndroidSourceSet

/**
 * Configures resource generation for standard Android targets (Application and Library).
 *
 * This function performs the "heavy lifting" for standard AGP projects:
 * 1. Maps the [KotlinSourceSet] to the corresponding [AndroidSourceSet].
 * 2. Injects the generated source directories into the Android [Variant] API.
 * 3. Handles nested components like unit tests or custom test fixtures.
 *
 * It relies on the variants collected by [setupAndroidVariantsSync] and ensures that
 * the [GenerateMultiplatformResourcesTask] is aware of the specific Android source
 * set it is serving.
 *
 * @param target The [KotlinTarget], expected to be a [KotlinAndroidTarget].
 * @param sourceSet The source set containing the resources.
 * @param genTaskProvider The provider for the generation task.
 * @param compilation The current Android-specific Kotlin compilation.
 */
@OptIn(ExperimentalKotlinGradlePluginApi::class)
internal fun setupAndroidTargetSources(
    target: KotlinTarget,
    sourceSet: KotlinSourceSet,
    genTaskProvider: TaskProvider<GenerateMultiplatformResourcesTask>,
    compilation: KotlinCompilation<*>,
) {
    if (target !is KotlinAndroidTarget) {
        return
    }

    val project: Project = target.project

    val androidCompilation = compilation as KotlinJvmAndroidCompilation

    // 1. Identify the corresponding Android source set (e.g., "main", "debug").
    val androidSourceSet: AndroidSourceSet = project.findAndroidSourceSet(
        kotlinSourceSet = sourceSet
    ) ?: throw GradleException("can't find android source set for $sourceSet")

    // Inform the task which Android source set it's working with.
    genTaskProvider.configure { it.androidSourceSetName.set(androidSourceSet.name) }

    // 2. Access the variants collected during the synchronization phase.
    @Suppress("UNCHECKED_CAST")
    val androidVariants: NamedDomainObjectContainer<Variant> = project.extra
        .get(VARIANTS_EXTRA_NAME) as NamedDomainObjectContainer<Variant>

    // 3. Wire generated sources into the AGP Variant API.
    androidVariants.configureEach { variant: Variant ->
        // Direct variant match (e.g., "debug" == "debug")
        if (variant.name == androidCompilation.name) {
            variant.sources.addLegacyAndroidGeneratedSources(genTaskProvider)
        }

        // Check nested components (e.g., unit tests associated with the variant)
        variant.nestedComponents.forEach { component ->
            if (component.name == androidCompilation.name) {
                component.sources.addLegacyAndroidGeneratedSources(genTaskProvider)
            }
        }
    }
}
