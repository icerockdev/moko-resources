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

const val VARIANTS_EXTRA_NAME = "dev.icerock.moko.resources.android-variants"

/**
 * Sets up Android-related wiring for the generated multiplatform resources task.
 *
 * Depending on the Android integration model used by the project, this function:
 *
 *  - registers generated sources, resources and assets for variants produced by
 *    the Kotlin Multiplatform Android plugin (`com.android.kotlin.multiplatform.library`);
 *  - registers generated sources and assets for the legacy Android plugin (`com.android.library`);
 *  - assigns the Android source set name to the generation task for correct variant scoping;
 *  - connects the generation task to the Android build lifecycle, including `preBuild`
 *    to prevent lint and resource-processing failures.
 *
 * Both modern and legacy Android configurations are supported:
 *
 *  - For AGP 8.10+, the new `onVariants` API is used.
 *  - For older AGP versions, the deprecated `onVariant` API is used.
 *  - When KMP Android integration is not present, classic `AndroidSourceSet` lookup is used.
 *
 * This function is invoked once per Kotlin source set participating in Android compilation.
 */
@OptIn(ExperimentalKotlinGradlePluginApi::class)
internal fun setupAndroidTasks(
    target: KotlinTarget,
    sourceSet: KotlinSourceSet,
    genTaskProvider: TaskProvider<GenerateMultiplatformResourcesTask>,
    compilation: KotlinCompilation<*>,
) {
    val project: Project = target.project

    println("DBG: target ${target.name} ${target.platformType.name} ${target.artifactsTaskName}")

    project.plugins.withId(AndroidLibraryType.Library.pluginId) {
        setupAndroidTargetSources(
            target = target,
            sourceSet = sourceSet,
            genTaskProvider = genTaskProvider,
            compilation = compilation
        )
    }

    project.plugins.withId(AndroidLibraryType.Application.pluginId) {
        setupAndroidTargetSources(
            target = target,
            sourceSet = sourceSet,
            genTaskProvider = genTaskProvider,
            compilation = compilation
        )
    }

    project.plugins.withId(AndroidLibraryType.KmpLibrary.pluginId) {
        setupAndroidMultiplatformLibraryTargetSources(
            target = target,
            genTaskProvider = genTaskProvider,
            compilation = compilation
        )
    }

    // Ensure generated resources are produced before Android's "preBuild" phase.
    // This avoids issues with lint, resource merging and packaging tasks.
    project.tasks
        .matching { it.name == "preBuild" }
        .configureEach { it.dependsOn(genTaskProvider) }
}

internal fun setupAndroidVariantsSync(project: Project) {
    androidPlugins().forEach { pluginId ->
        project.plugins.withId(pluginId) {
            val androidVariants: NamedDomainObjectContainer<Variant> =
                project.objects.domainObjectContainer(Variant::class.java)

            project.extra.set(VARIANTS_EXTRA_NAME, androidVariants)

            val componentsExtension: AndroidComponentsExtension<*, *, *> =
                project.extensions.findByType(LibraryAndroidComponentsExtension::class.java)
                    ?: project.extensions.findByType(ApplicationAndroidComponentsExtension::class.java)
                    ?: project.extensions.findByType(AndroidComponentsExtension::class.java)
                    ?: error("can't find AndroidComponentsExtension")

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
