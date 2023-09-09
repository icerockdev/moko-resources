/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.configuration

import dev.icerock.gradle.generator.MRGenerator
import dev.icerock.gradle.generator.ResourceGeneratorFeature
import dev.icerock.gradle.generator.apple.AppleMRGenerator
import dev.icerock.gradle.tasks.CopyXCFrameworkResourcesToApp
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFrameworkTask
import org.jetbrains.kotlin.konan.target.HostManager

internal fun configureAppleTargetGenerator(
    target: KotlinNativeTarget,
    settings: MRGenerator.Settings,
    features: List<ResourceGeneratorFeature<out MRGenerator.Generator>>
) {
    if (HostManager.hostIsMac.not()) {
        target.project.logger.warn("MR file generation for Apple is not supported on your system!")
        return
    }

    val mainCompilation: KotlinNativeCompilation = target.compilations
        .getByName(KotlinCompilation.MAIN_COMPILATION_NAME)

    AppleMRGenerator(
        generatedDir = settings.generatedDir,
        sourceSet = target.project.provider { createSourceSet(mainCompilation.defaultSourceSet) },
        settings = settings,
        generators = features.map { it.createIosGenerator() },
        compilation = mainCompilation,
        baseLocalizationRegion = settings.iosLocalizationRegion
    ).apply(target.project)

    setupCopyXCFrameworkResourcesTask(project = target.project)
}

private fun setupCopyXCFrameworkResourcesTask(project: Project) {
    // Seems that there were problem with this block in the past with mystic task adding. Need more info
    // Now, that works perfectly, I've tested on the real project with Kotlin 1.9.10 and KSP enabled
    // Suppose that on that moment there were no lazy register method for task container
    project.tasks.withType(XCFrameworkTask::class).configureEach { task ->
        val copyTaskName: String = task.name
            .replace("assemble", "copyResources").plus("ToApp")

        project.tasks.register(copyTaskName, CopyXCFrameworkResourcesToApp::class.java) {
            it.xcFrameworkDir = task.outputDir
            it.dependsOn(task)
        }
    }
}
