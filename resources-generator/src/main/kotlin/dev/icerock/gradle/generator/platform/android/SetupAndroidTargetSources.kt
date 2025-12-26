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

    // Legacy Android Plugin (com.android.library).
    // Identify the corresponding Android source set.
    val androidSourceSet: AndroidSourceSet = project.findAndroidSourceSet(
        kotlinSourceSet = sourceSet
    ) ?: throw GradleException("can't find android source set for $sourceSet")

    // Assign the Android source set name to the task
    // (used for skipping build-type-specific tasks).
    genTaskProvider.configure { it.androidSourceSetName.set(androidSourceSet.name) }

    // Wire generated sources into AGP's legacy Variant API.
    val androidVariants: NamedDomainObjectContainer<Variant> = project.extra
        .get(VARIANTS_EXTRA_NAME) as NamedDomainObjectContainer<Variant>

    androidVariants.configureEach { variant: Variant ->
        // Attach directories at the variant level
        if (variant.name == androidCompilation.name) {
            variant.sources.addLegacyAndroidGeneratedSources(genTaskProvider)
        }

        // Attach also to nested components (flavors/build-types).
        variant.nestedComponents.forEach { component ->
            if (component.name == androidCompilation.name) {
                component.sources.addLegacyAndroidGeneratedSources(genTaskProvider)
            }
        }
    }
}
