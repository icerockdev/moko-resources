/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.platform.apple

import com.android.build.gradle.internal.tasks.factory.dependsOn
import dev.icerock.gradle.MultiplatformResourcesPluginExtension
import dev.icerock.gradle.actions.apple.CopyResourcesFromFrameworkToFatAction
import dev.icerock.gradle.actions.apple.CopyResourcesFromKLibsToExecutableAction
import dev.icerock.gradle.actions.apple.CopyResourcesFromKLibsToFrameworkAction
import dev.icerock.gradle.actions.apple.PackAppleResourcesToKLibAction
import dev.icerock.gradle.tasks.CopyFrameworkResourcesToAppTask
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.TestExecutable
import org.jetbrains.kotlin.gradle.tasks.FatFrameworkTask
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile
import java.io.File

internal fun setupAppleKLibResources(
    compileTask: KotlinNativeCompile,
    assetsDirectory: Provider<File>,
    resourcesGenerationDir: Provider<File>,
    iosLocalizationRegion: Provider<String>,
    resourcePackageName: Provider<String>,
    acToolMinimalDeploymentTarget: Provider<String>
) {
    compileTask.doLast(
        PackAppleResourcesToKLibAction(
            baseLocalizationRegion = iosLocalizationRegion,
            resourcePackageName = resourcePackageName,
            assetsDirectory = assetsDirectory,
            resourcesGenerationDir = resourcesGenerationDir,
            acToolMinimalDeploymentTarget = acToolMinimalDeploymentTarget
        )
    )

    // tasks like compileIosMainKotlinMetadata when only one target enabled
//        generationTask.project.tasks
//            .withType<KotlinCommonCompile>()
//            .matching { it.name.contains(sourceSet.name, ignoreCase = true) }
//            .configureEach { it.dependsOn(generationTask) }

    //TODO fix usage of sourceSet
//        dependsOnProcessResources(
//            project = generationTask.project,
//            sourceSet = sourceSet,
//            task = generationTask,
//        )
}

internal fun setupFrameworkResources(compilation: KotlinNativeCompilation) {
    compilation.target.binaries.withType<Framework>().configureEach { framework ->
        framework.linkTaskProvider.configure { linkTask ->
            linkTask.doLast(CopyResourcesFromKLibsToFrameworkAction())
        }

        if (framework.isStatic) {
            val project: Project = framework.project
            val resourcesExtension: MultiplatformResourcesPluginExtension =
                project.extensions.getByType()
            if (resourcesExtension.staticFrameworkWarningEnabled.get()) {
                project.logger.warn(
                    """
${framework.linkTaskName} produces static framework, Xcode should have Build Phase with copyFrameworkResourcesToApp gradle task call. Please read readme on https://github.com/icerockdev/moko-resources
"""
                )
            }
            createCopyFrameworkResourcesTask(framework)
        }
    }
}

internal fun createCopyFrameworkResourcesTask(framework: Framework) {
    val project: Project = framework.project
    val taskName: String = framework.linkTaskName.replace("link", "copyResources")

    val copyTask: TaskProvider<CopyFrameworkResourcesToAppTask> =
        project.tasks.register(taskName, CopyFrameworkResourcesToAppTask::class.java) {
            it.inputFrameworkDirectory.set(framework.outputDirectoryProperty)
            it.outputDirectory.set(project.provider {
                val buildProductsDir =
                    project.property("moko.resources.BUILT_PRODUCTS_DIR") as String
                val contentsFolderPath =
                    project.property("moko.resources.CONTENTS_FOLDER_PATH") as String

                val targetDir = File("$buildProductsDir/$contentsFolderPath")
                val baseDir: File = project.layout.projectDirectory.asFile

                project.layout.projectDirectory.dir(targetDir.relativeTo(baseDir).path)
            })
        }
    copyTask.dependsOn(framework.linkTaskProvider)

    //TODO: Вынести в отдельную таску, должно создаваться один раз
//    val xcodeTask = project.tasks.maybeCreate(
//        "copyFrameworkResourcesToApp",
//        CopyFrameworkResourcesToAppEntryPointTask::class.java
//    )
//    val multiplatformExtension = project.extensions.getByType<KotlinMultiplatformExtension>()
//    xcodeTask.configurationMapper = (multiplatformExtension as? ExtensionAware)?.extensions
//        ?.findByType<CocoapodsExtension>()
//        ?.xcodeConfigurationToNativeBuildType
//        ?: emptyMap()
//
//    if (framework.target.konanTarget == xcodeTask.konanTarget &&
//        framework.buildType.getName() == xcodeTask.configuration?.lowercase()
//    ) {
//        xcodeTask.dependsOn(copyTask)
//    }
}

internal fun setupTestsResources(compilation: KotlinNativeCompilation) {
    compilation.target.binaries.withType<TestExecutable>().configureEach { executable ->
        executable.linkTaskProvider.configure { link ->
            link.doLast(CopyResourcesFromKLibsToExecutableAction())
        }
    }
}

internal fun setupFatFrameworkTasks(compilation: KotlinNativeCompilation) {
    compilation.project.tasks.withType<FatFrameworkTask>().configureEach {
        @Suppress("UNCHECKED_CAST")
        it.doLast(CopyResourcesFromFrameworkToFatAction() as Action<Task>)
    }
}
