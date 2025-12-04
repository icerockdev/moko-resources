/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.platform.android

import com.android.build.api.dsl.AndroidSourceSet
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import com.android.build.api.extension.impl.CurrentAndroidGradlePluginVersion
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.KotlinMultiplatformAndroidComponentsExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.api.variant.Sources
import com.android.build.api.variant.Variant
import com.android.build.gradle.BaseExtension
import dev.icerock.gradle.tasks.GenerateMultiplatformResourcesTask
import org.gradle.api.GradleException
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.findByType
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJvmAndroidCompilation
import org.jetbrains.kotlin.gradle.plugin.sources.android.androidSourceSetInfoOrNull
import org.jetbrains.kotlin.gradle.plugin.sources.android.findAndroidSourceSet

private const val VARIANTS_EXTRA_NAME = "dev.icerock.moko.resources.android-variants"

@OptIn(ExperimentalKotlinGradlePluginApi::class)
internal fun setupAndroidTasks(
    target: KotlinTarget,
    sourceSet: KotlinSourceSet,
    genTaskProvider: TaskProvider<GenerateMultiplatformResourcesTask>,
    compilation: KotlinCompilation<*>,
) {
    if (target is KotlinAndroidTarget) {
        compilation as KotlinJvmAndroidCompilation

        val project: Project = target.project

        val androidSourceSet: AndroidSourceSet = project.findAndroidSourceSet(sourceSet)
            ?: throw GradleException("can't find android source set for $sourceSet")

        // save android sourceSet name to skip build type specific tasks
        genTaskProvider.configure { it.androidSourceSetName.set(androidSourceSet.name) }

        // connect generateMR task with android tasks
        val androidVariants: NamedDomainObjectContainer<Variant> = project.extra
            .get(VARIANTS_EXTRA_NAME) as NamedDomainObjectContainer<Variant>

        androidVariants
            .configureEach { variant ->
                if (variant.name == compilation.name) {
                    variant.sources.addGenerationTaskDependency(genTaskProvider)
                }

                variant.nestedComponents.forEach { component ->
                    if (component.name == compilation.name) {
                        component.sources.addGenerationTaskDependency(genTaskProvider)
                    }
                }
            }

        // to fix issues with android lint - depends on preBuild
        project.tasks
            .matching { it.name == "preBuild" }
            .configureEach { it.dependsOn(genTaskProvider) }
    }
    if (target is KotlinMultiplatformAndroidLibraryTarget) {
        val project: Project = target.project
        project.isAgpVersionGreaterOrEqual(1, 10)
        project.extensions.extensionsSchema.forEach {
            project.logger.warn("project.extensions name=${it.name}")
        }
        val componentsExtension =
            project.extensions.findByType<KotlinMultiplatformAndroidComponentsExtension>()
        val androidExtension =
            project.extensions.getByName("androidComponents") as KotlinMultiplatformAndroidComponentsExtension
        project.logger.warn("componentsExtension: $componentsExtension")
        androidExtension.onVariants { variant ->
            project.logger.warn("componentsExtension.onVariants: ${variant.name}")

            // 3. У варианта есть доступ к его source-set'ам!
            // variant.sources - это объект типа Sources, который нам и нужен.
            val androidSources = variant.sources
            androidSources.addGenerationTaskDependency(genTaskProvider)
            project.logger.warn("nestedComponents size: ${variant.nestedComponents.size}")
            variant.sources.addGenerationTaskDependency(genTaskProvider)
            try {
                // Используем имя варианта, оно совпадает с именем sourceSet
                genTaskProvider.configure { it.androidSourceSetName.set(variant.name) }
                project.logger.warn("genTaskProvider.configure: ${variant.name}")
            } catch (exception: Exception) {
                target.project.logger.warn("androidJvm: error configure genTaskProvider $exception")
            }
        }
        // to fix issues with android lint - depends on preBuild
        project.tasks
            .matching { it.name == "preBuild" }
            .configureEach { it.dependsOn(genTaskProvider) }
    }
}

internal fun Sources.addGenerationTaskDependency(provider: TaskProvider<GenerateMultiplatformResourcesTask>) {
    kotlin?.addGeneratedSourceDirectory(
        taskProvider = provider,
        wiredWith = GenerateMultiplatformResourcesTask::outputSourcesDir
    )

    // Resources doesn't add in android variants for IDE indexing
    // Resource directory set here:
    // dev.icerock.gradle.MultiplatformResourcesPlugin.setupSourceSets
    //    res?.addGeneratedSourceDirectory(
    //        taskProvider = provider,
    //        wiredWith = GenerateMultiplatformResourcesTask::outputResourcesDir
    //    )

    // Assets add here, for correct compilation
    assets?.addGeneratedSourceDirectory(
        taskProvider = provider,
        wiredWith = GenerateMultiplatformResourcesTask::outputAssetsDir
    )
}

internal fun setupAndroidVariantsSync(project: Project) {
    androidPlugins().forEach { pluginId ->
        project.plugins.withId(pluginId) {
            val androidVariants: NamedDomainObjectContainer<Variant> =
                project.objects.domainObjectContainer(Variant::class.java)

            project.extra.set(VARIANTS_EXTRA_NAME, androidVariants)

            val componentsExtension: AndroidComponentsExtension<*, *, *> = project.extensions
                .findByType<LibraryAndroidComponentsExtension>()
                ?: project.extensions.findByType<ApplicationAndroidComponentsExtension>()
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

// Можно добавить этот код в конец файла SetupAndroidUtils.kt

// В файле SetupAndroidUtils.kt

private fun Project.isAgpVersionGreaterOrEqual(major: Int, minor: Int): Boolean {
    var agpVersion: String? = null
    logger.warn("MokoResources: AndroidGradlePluginVersion ${CurrentAndroidGradlePluginVersion.CURRENT_AGP_VERSION}")
    return true
}
