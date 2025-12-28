/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.platform.android

import com.android.build.api.dsl.AndroidSourceSet
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.api.variant.Variant
import com.android.build.gradle.BaseExtension
import dev.icerock.gradle.tasks.GenerateMultiplatformResourcesTask
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.findByType
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.sources.android.androidSourceSetInfoOrNull

internal const val VARIANTS_EXTRA_NAME = "dev.icerock.moko.resources.android-variants"

/**
 * Orchestrates Android-specific wiring for multiplatform resource generation.
 *
 * This function integrates the [GenerateMultiplatformResourcesTask] into the Android
 * build pipeline based on the applied plugins:
 *
 * - **Standard Android Plugins** (`com.android.application` / `com.android.library`):
 * Registers generated sources and assets via the standard Android target logic.
 * - **KMP Android Plugin** (`com.android.kotlin.multiplatform.library`):
 * Integrates with the Kotlin Multiplatform Android variant API.
 *
 * Additionally, it ensures that resource generation occurs before the `preBuild`
 * phase to prevent issues with Lint, resource merging, and packaging.
 *
 * @param target The [KotlinTarget] being configured.
 * @param sourceSet The specific [KotlinSourceSet] participating in the Android compilation.
 * @param genTaskProvider The provider for the resource generation task.
 * @param compilation The [KotlinCompilation] associated with this setup.
 */
@OptIn(ExperimentalKotlinGradlePluginApi::class)
internal fun setupAndroidTasks(
    target: KotlinTarget,
    sourceSet: KotlinSourceSet,
    genTaskProvider: TaskProvider<GenerateMultiplatformResourcesTask>,
    compilation: KotlinCompilation<*>,
) {
    val project: Project = target.project

    // 1. Setup for Standard AGP (Application or Library)
    if (project.hasAndroidApplicationPlugin() || project.hasAndroidLibraryPlugin()) {
        setupAndroidTargetSources(
            target = target,
            sourceSet = sourceSet,
            genTaskProvider = genTaskProvider,
            compilation = compilation
        )
    }

    // 2. Setup for Kotlin Multiplatform Android Library
    if (project.hasAndroidKmpLibraryPlugin()) {
        setupAndroidMultiplatformLibraryTargetSources(
            target = target,
            genTaskProvider = genTaskProvider,
            compilation = compilation
        )
    }

    // Wiring to preBuild:
    // This is crucial for Android as it ensures resources are ready before Lint or
    // any code analysis tasks start looking for them.
    project.tasks
        .matching { it.name == "preBuild" }
        .configureEach { it.dependsOn(genTaskProvider) }
}

/**
 * Synchronizes Android variants by collecting them into a observable container.
 *
 * Since Android variants are created and configured during the late stages of
 * the Gradle configuration phase, this function uses the [AndroidComponentsExtension.onVariants]
 * callback to capture each [Variant] object as it becomes available.
 *
 * The collected variants are stored in a [NamedDomainObjectContainer] within the
 * project's extra properties ([VARIANTS_EXTRA_NAME]). This allows other parts of
 * the plugin to react to or query Android variants without directly depending on
 * the AGP extension in every call site.
 *
 * @param project The Gradle project to configure.
 * @throws org.gradle.api.GradleException if the [AndroidComponentsExtension] cannot be resolved
 * after an Android plugin has been applied.
 */
internal fun setupAndroidVariantsSync(project: Project) {
    androidPlugins().forEach { pluginId ->
        project.plugins.withId(pluginId) {
            // Create a container to store variants lazily
            val androidVariants: NamedDomainObjectContainer<Variant> =
                project.objects.domainObjectContainer(Variant::class.java)

            // Store the container in extra properties for cross-plugin/task access
            project.extra.set(VARIANTS_EXTRA_NAME, androidVariants)

            val componentsExtension: AndroidComponentsExtension<*, *, *> =
                project.extensions.findByType(LibraryAndroidComponentsExtension::class.java)
                    ?: project.extensions.findByType(ApplicationAndroidComponentsExtension::class.java)
                    ?: project.extensions.findByType(AndroidComponentsExtension::class.java)
                    ?: error("can't find AndroidComponentsExtension")

            // Register the callback to populate our container
            componentsExtension.onVariants { variant: Variant ->
                androidVariants.add(variant)
            }
        }
    }
}

/**
 * Replace of ExperimentalKotlinGradlePluginApi in AGP
 * Current realisation in plugin use of Deprecated version AndroidSourceSet
 */
@ExperimentalKotlinGradlePluginApi
@Suppress("ReturnCount")
internal fun Project.getAndroidSourceSetOrNull(kotlinSourceSet: KotlinSourceSet): AndroidSourceSet? {
    val androidSourceSetInfo = kotlinSourceSet.androidSourceSetInfoOrNull ?: return null
    val android = extensions.findByType<BaseExtension>() ?: return null
    return android.sourceSets.getByName(androidSourceSetInfo.androidSourceSetName)
}
