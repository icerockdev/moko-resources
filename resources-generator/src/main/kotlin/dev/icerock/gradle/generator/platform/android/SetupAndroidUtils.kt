/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.platform.android

import com.android.build.api.dsl.AndroidSourceSet
import com.android.build.api.dsl.KotlinMultiplatformAndroidCompilation
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
import dev.icerock.gradle.utils.hasMinimalVersion
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

    // Only Android targets (legacy or KMP) participate in Android resource wiring.
    if (target !is KotlinAndroidTarget && target !is KotlinMultiplatformAndroidLibraryTarget) return

    val androidExtension: KotlinMultiplatformAndroidComponentsExtension? = project.extensions
        .findByType<KotlinMultiplatformAndroidComponentsExtension>()

    if (androidExtension != null) {
        // Modern KMP Android integration:
        // Use the best available variant API depending on the AGP version.
        val hasMinimalVersionAgp: Boolean = hasMinimalVersion(
            minVersion = AGP_8_10_0,
            currentVersion = CurrentAndroidGradlePluginVersion.CURRENT_AGP_VERSION.version
        )

        if (hasMinimalVersionAgp) {
            // AGP 8.10+: new unified variant API
            androidExtension.onVariants { variant ->
                variantHandler(
                    variant = variant,
                    genTaskProvider = genTaskProvider,
                    compilation = compilation
                )
            }
        } else {
            // Older AGP versions still expose onVariant() (deprecated).
            @Suppress("DEPRECATION")
            androidExtension.onVariant { variant ->
                variantHandler(
                    variant = variant,
                    genTaskProvider = genTaskProvider,
                    compilation = compilation
                )
            }
        }
    }

    if (androidExtension == null && target is KotlinAndroidTarget) {
        val androidCompilation = compilation as KotlinJvmAndroidCompilation

        // Legacy Android Plugin (com.android.library).
        // Identify the corresponding Android source set.
        val androidSourceSet: AndroidSourceSet = project.findAndroidSourceSet(
            kotlinSourceSet = sourceSet
        ) ?: throw GradleException("can't find android source set for $sourceSet")

        // Assign the Android source set name to the task (used for skipping build-type-specific tasks).
        genTaskProvider.configure { it.androidSourceSetName.set(androidSourceSet.name) }

        // Wire generated sources into AGP's legacy Variant API.
        val androidVariants: NamedDomainObjectContainer<Variant> = project.extra
            .get(VARIANTS_EXTRA_NAME) as NamedDomainObjectContainer<Variant>

        androidVariants.configureEach { variant ->
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

    // Ensure generated resources are produced before Android's "preBuild" phase.
    // This avoids issues with lint, resource merging and packaging tasks.
    project.tasks
        .matching { it.name == "preBuild" }
        .configureEach { it.dependsOn(genTaskProvider) }
}

/**
 * Registers generated Kotlin sources, Java sources, resources and assets for Android targets
 * when using the Kotlin Multiplatform Android plugin (`com.android.kotlin.multiplatform.library`).
 *
 * In this integration model, Android units of code are exposed through both Kotlin and Java
 * source sets. To ensure that generated directories are consistently discovered by the IDE and
 * the build system, the generated Kotlin sources are registered in both source sets.
 *
 * This function provides the full set of generated directories required for proper indexing
 * and compilation under the KMP Android plugin.
 */
internal fun Sources.addKmpAndroidGeneratedSources(
    provider: TaskProvider<GenerateMultiplatformResourcesTask>
) {
    kotlin?.addGeneratedSourceDirectory(
        taskProvider = provider,
        wiredWith = GenerateMultiplatformResourcesTask::outputSourcesDir
    )

    // Generated Kotlin sources must also be added to the Java source set so that
    // the IDE and build system treat the generated directory as part of the Android target.
    // Note: this behavior may become incompatible with AGP 10+, according to guidance
    // shared by the AGP team in public issue tracker discussions.
    java?.addGeneratedSourceDirectory(
        taskProvider = provider,
        wiredWith = GenerateMultiplatformResourcesTask::outputSourcesDir
    )

    assets?.addGeneratedSourceDirectory(
        taskProvider = provider,
        wiredWith = GenerateMultiplatformResourcesTask::outputAssetsDir
    )

    res?.addGeneratedSourceDirectory(
        taskProvider = provider,
        wiredWith = GenerateMultiplatformResourcesTask::outputResourcesDir
    )
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

/**
 * Configures a single Android variant for use with generated resources when building under the
 * Kotlin Multiplatform Android plugin.
 *
 * For variants associated with a `KotlinMultiplatformAndroidCompilation`, this function:
 *  - registers all generated source, resource, and asset directories into the variant's
 *    source sets via [addKmpAndroidGeneratedSources];
 *  - configures the resource generation task with the variant’s Android source set name.
 *
 * Only the variant whose name matches the compilation’s component name is configured.
 * Errors during task configuration are logged but do not fail the build.
 */
private fun variantHandler(
    variant: Variant,
    genTaskProvider: TaskProvider<GenerateMultiplatformResourcesTask>,
    compilation: KotlinCompilation<*>,
) {
    if (compilation !is KotlinMultiplatformAndroidCompilation) return

    if (variant.name == compilation.componentName) {
        variant.sources.addKmpAndroidGeneratedSources(genTaskProvider)

        genTaskProvider.configure {
            it.androidSourceSetName.set(variant.name)
        }
    }
}

/**
 * Registers generated Kotlin sources and assets for Android variants when using the
 * classic AGP plugin (`com.android.library`).
 *
 * The generated resource directory (`res`) is not attached here; it is added later during
 * source set configuration, as required by the legacy AGP source set wiring model.
 *
 * Use this function only with the traditional Android plugin.
 */
internal fun Sources.addLegacyAndroidGeneratedSources(
    provider: TaskProvider<GenerateMultiplatformResourcesTask>
) {
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
