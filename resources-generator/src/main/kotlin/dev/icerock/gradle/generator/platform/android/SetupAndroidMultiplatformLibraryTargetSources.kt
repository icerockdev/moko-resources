/*
 * Copyright 2025 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.platform.android

import com.android.build.api.dsl.KotlinMultiplatformAndroidCompilation
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import com.android.build.api.extension.impl.CurrentAndroidGradlePluginVersion
import com.android.build.api.variant.KotlinMultiplatformAndroidComponentsExtension
import com.android.build.api.variant.KotlinMultiplatformAndroidVariant
import com.android.build.api.variant.Variant
import dev.icerock.gradle.tasks.GenerateMultiplatformResourcesTask
import dev.icerock.gradle.utils.hasMinimalVersion
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.findByType
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget

/**
 * Configures the source sets and task dependencies for the Android target within
 * a Kotlin Multiplatform Library project.
 *
 * This function specifically integrates the [GenerateMultiplatformResourcesTask]
 * into the Android variant lifecycle, ensuring that generated resources are
 * correctly recognized by the Android build system.
 *
 * ### AGP Compatibility
 * To support multiple versions of the Android Gradle Plugin, this function:
 * - Uses the unified variant API available in **AGP 8.10.0** and later.
 * - Falls back to the deprecated `onVariant` API for older AGP versions.
 *
 * @param target The [KotlinTarget] to configure. If the target is not a
 * [KotlinMultiplatformAndroidLibraryTarget], this function will return early.
 * @param genTaskProvider The [TaskProvider] for the resource generation task
 * that needs to be wired to the Android variants.
 * @param compilation The [KotlinCompilation] associated with the current target.
 * * @throws GradleException If the [KotlinMultiplatformAndroidComponentsExtension]
 * is missing in a project applying the `com.android.kotlin.multiplatform.library` plugin.
 */
fun setupAndroidMultiplatformLibraryTargetSources(
    target: KotlinTarget,
    genTaskProvider: TaskProvider<GenerateMultiplatformResourcesTask>,
    compilation: KotlinCompilation<*>,
) {
    if (target !is KotlinMultiplatformAndroidLibraryTarget) {
        return
    }

    val androidExtension: KotlinMultiplatformAndroidComponentsExtension =
        target.project.extensions.findByType() ?: throw GradleException(
            "KotlinMultiplatformAndroidComponentsExtension not found " +
                "in project with 'com.android.kotlin.multiplatform.library'"
        )

    val hasNewVariantApi: Boolean = hasMinimalVersion(
        minVersion = AGP_8_10_0,
        currentVersion = CurrentAndroidGradlePluginVersion.CURRENT_AGP_VERSION.version
    )

    if (hasNewVariantApi) {
        androidExtension.onVariants { variant: KotlinMultiplatformAndroidVariant ->
            variantHandler(
                variant = variant,
                genTaskProvider = genTaskProvider,
                compilation = compilation
            )
        }
    } else {
        @Suppress("DEPRECATION")
        androidExtension.onVariant { variant: KotlinMultiplatformAndroidVariant ->
            variantHandler(
                variant = variant,
                genTaskProvider = genTaskProvider,
                compilation = compilation
            )
        }
    }
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
