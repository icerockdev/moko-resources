/*
 * Copyright 2025 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.platform.android

import com.android.build.api.variant.Sources
import dev.icerock.gradle.tasks.GenerateMultiplatformResourcesTask
import org.gradle.api.tasks.TaskProvider

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
