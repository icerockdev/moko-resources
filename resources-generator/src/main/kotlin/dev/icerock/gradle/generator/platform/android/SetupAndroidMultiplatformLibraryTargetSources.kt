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

    // Modern KMP Android integration:
    // Use the best available variant API depending on the AGP version.
    val hasMinimalVersionAgp: Boolean = hasMinimalVersion(
        minVersion = AGP_8_10_0,
        currentVersion = CurrentAndroidGradlePluginVersion.CURRENT_AGP_VERSION.version
    )

    if (hasMinimalVersionAgp) {
        // AGP 8.10+: new unified variant API
        androidExtension.onVariants { variant: KotlinMultiplatformAndroidVariant ->
            variantHandler(
                variant = variant,
                genTaskProvider = genTaskProvider,
                compilation = compilation
            )
        }
    } else {
        // Older AGP versions still expose onVariant() (deprecated).
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
