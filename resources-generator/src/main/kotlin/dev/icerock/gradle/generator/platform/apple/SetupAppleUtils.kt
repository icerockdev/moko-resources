/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.platform.apple

import dev.icerock.gradle.actions.apple.CopyAppleResourcesFromFrameworkToFatAction
import dev.icerock.gradle.actions.apple.CopyResourcesFromKLibsToExecutableAction
import dev.icerock.gradle.actions.apple.CopyResourcesFromKLibsToFrameworkAction
import dev.icerock.gradle.actions.apple.PackAppleResourcesToKLibAction
import dev.icerock.gradle.tasks.CopyExecutableResourcesToApp
import dev.icerock.gradle.tasks.CopyFrameworkResourcesToAppTask
import dev.icerock.gradle.tasks.CopyXCFrameworkResourcesToApp
import dev.icerock.gradle.utils.AppleSdk
import dev.icerock.gradle.utils.capitalize
import dev.icerock.gradle.utils.disableStaticFrameworkWarning
import dev.icerock.gradle.utils.klibs
import dev.icerock.gradle.utils.nameWithoutBuildType
import dev.icerock.gradle.utils.propertyString
import dev.icerock.gradle.utils.propertyStrings
import org.gradle.api.Action
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.Directory
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.cocoapods.CocoapodsExtension
import org.jetbrains.kotlin.gradle.plugin.cocoapods.KotlinCocoapodsPlugin
import org.jetbrains.kotlin.gradle.plugin.mpp.AbstractExecutable
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.plugin.mpp.TestExecutable
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFrameworkTask
import org.jetbrains.kotlin.gradle.tasks.FatFrameworkTask
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile
import java.io.File

@Suppress("LongParameterList")
internal fun setupAppleKLibResources(
    compileTask: KotlinNativeCompile,
    assetsDirectory: Provider<File>,
    resourcesGenerationDir: Provider<File>,
    iosLocalizationRegion: Provider<String>,
    appleBundleIdentifier: Provider<String>,
    iosMinimalDeploymentTarget: Provider<String>,
) {
    compileTask.doLast(
        PackAppleResourcesToKLibAction(
            baseLocalizationRegion = iosLocalizationRegion,
            bundleIdentifier = appleBundleIdentifier,
            assetsDirectory = assetsDirectory,
            resourcesGenerationDir = resourcesGenerationDir,
            iosMinimalDeploymentTarget = iosMinimalDeploymentTarget
        )
    )
}

internal fun setupFrameworkResources(
    target: KotlinNativeTarget,
) {
    target.binaries.withType<Framework>().configureEach { framework ->
        framework.linkTaskProvider.configure { linkTask ->
            linkTask.doLast(CopyResourcesFromKLibsToFrameworkAction())
        }

        val project: Project = framework.project
        if (project.disableStaticFrameworkWarning.not()) {
            project.logger.warn(
                """
                |${framework.linkTaskName} is found.
                |If you use a static framework, Xcode should have Build Phase with copy${framework.nameWithoutBuildType.capitalize()}FrameworkResourcesToApp gradle task call. 
                |Please read readme on https://github.com/icerockdev/moko-resources
                |-
                |To hide this message, add 'moko.resources.disableStaticFrameworkWarning=true' to the Gradle properties.
                
                """.trimMargin()
            )
        }

        createCopyFrameworkResourcesTask(framework)
    }
}

internal fun createCopyFrameworkResourcesTask(framework: Framework) {
    val project: Project = framework.project
    val taskName: String = framework.linkTaskName.replace("link", "copyResources")

    project.tasks.register(
        /* name = */ taskName,
        /* type = */ CopyFrameworkResourcesToAppTask::class.java
    ) { task ->
        task.configuration = framework.buildType.name
        task.konanTarget = framework.compilation.konanTarget.name
        task.frameworkPrefix = framework.nameWithoutBuildType

        task.frameworkIsStatic.set(
            project.provider {
                framework.isStatic
            }
        )
        val projectDirectory: Directory = project.layout.projectDirectory
        task.inputFrameworkDirectory.set(
            framework.linkTaskProvider
                .flatMap { it.outputFile }
                .let { project.layout.dir(it) }
        )
        task.outputDirectory.set(
            project.provider {
                val buildProductsDir =
                    project.property("moko.resources.BUILT_PRODUCTS_DIR") as String
                val contentsFolderPath =
                    project.property("moko.resources.CONTENTS_FOLDER_PATH") as String

                val targetDir = File("$buildProductsDir/$contentsFolderPath")
                val baseDir: File = projectDirectory.asFile

                projectDirectory.dir(targetDir.relativeTo(baseDir).path)
            }
        )

        task.dependsOn(framework.linkTaskProvider)
    }
}

internal fun registerCopyFrameworkResourcesToAppTask(
    project: Project,
) {
    val configuration: String? = project.propertyString(
        name = KotlinCocoapodsPlugin.CONFIGURATION_PROPERTY
    ) ?: project.propertyString(
        name = "moko.resources.CONFIGURATION"
    )
    val platform: String? = project.propertyString(
        name = KotlinCocoapodsPlugin.PLATFORM_PROPERTY
    ) ?: project.propertyString(
        name = "moko.resources.PLATFORM_NAME"
    )
    val archs: List<String>? = project.propertyStrings(
        name = KotlinCocoapodsPlugin.ARCHS_PROPERTY
    ) ?: project.propertyStrings(
        name = "moko.resources.ARCHS"
    )

    if (platform == null || archs == null || configuration == null) return

    val kotlinMultiplatformExtension = project.extensions.getByType<KotlinMultiplatformExtension>()
    val frameworkNames: DomainObjectSet<String> =
        project.objects.domainObjectSet(String::class.java)

    kotlinMultiplatformExtension.targets.withType<KotlinNativeTarget>().configureEach { target ->
        target.binaries.withType<Framework>().configureEach {
            frameworkNames.add(it.nameWithoutBuildType)
        }
    }

    frameworkNames.configureEach { frameworkPrefix ->
        val xcodeTask: TaskProvider<Task> = project.tasks.register(
            "copy${frameworkPrefix.capitalize()}FrameworkResourcesToApp"
        )

        xcodeTask.configure {
            val configMap: Map<String, NativeBuildType> =
                (kotlinMultiplatformExtension as? ExtensionAware)
                    ?.extensions
                    ?.findByType<CocoapodsExtension>()
                    ?.xcodeConfigurationToNativeBuildType
                    ?: emptyMap()

            val configName: String = (configMap[configuration]?.name ?: configuration).lowercase()
            val requiredKonanTargets: List<String> =
                AppleSdk.defineNativeTargets(platform, archs).map { it.name }

            it.dependsOn(
                project.tasks.withType<CopyFrameworkResourcesToAppTask>().matching { copyTask ->
                    val isCorrectConfiguration: Boolean =
                        copyTask.configuration.lowercase() == configName
                    val isCorrectFrameworkPrefix: Boolean =
                        copyTask.frameworkPrefix == frameworkPrefix
                    val isCorrectKonanTarget: Boolean =
                        requiredKonanTargets.contains(copyTask.konanTarget)

                    isCorrectConfiguration && isCorrectFrameworkPrefix && isCorrectKonanTarget
                }
            )
        }
    }
}

internal fun registerCopyXCFrameworkResourcesToAppTask(
    project: Project,
    xcFrameworkName: String
) {
    val copyTaskName: String = "copyResources" + xcFrameworkName + "ToApp"

    project.tasks.register<CopyXCFrameworkResourcesToApp>(copyTaskName) {
        val xcFrameworkTask = this.project.tasks
            .getByName("assemble$xcFrameworkName") as XCFrameworkTask

        xcFrameworkDir.set(xcFrameworkTask.outputDir)
        outputDir.set(
            project.layout.dir(
                project.provider {
                    val buildProductsDir =
                        project.property("moko.resources.BUILT_PRODUCTS_DIR") as String
                    val contentsFolderPath =
                        project.property("moko.resources.CONTENTS_FOLDER_PATH") as String

                    File("$buildProductsDir/$contentsFolderPath")
                }
            )
        )
        dependsOn(xcFrameworkTask)
    }
}

internal fun setupExecutableResources(target: KotlinNativeTarget) {
    val project: Project = target.project
    target.binaries.withType<AbstractExecutable>().configureEach { executable ->
        val copyTaskName: String =
            executable.linkTaskProvider.name.replace("link", "copyResources")

        project.tasks.register<CopyExecutableResourcesToApp>(copyTaskName) {
            dependsOn(executable.linkTaskProvider)

            klibs.from(executable.linkTaskProvider.map { it.klibs })

            outputDirectory.set(
                project.layout.dir(
                    project.provider {
                        val buildProductsDir =
                            project.property("moko.resources.BUILT_PRODUCTS_DIR") as String
                        val contentsFolderPath =
                            project.property("moko.resources.CONTENTS_FOLDER_PATH") as String

                        File("$buildProductsDir/$contentsFolderPath")
                    }
                )
            )
        }
    }
}

internal fun setupTestsResources(target: KotlinNativeTarget) {
    target.binaries.withType<TestExecutable>().configureEach { executable ->
        executable.linkTaskProvider.configure { link ->
            link.doLast(CopyResourcesFromKLibsToExecutableAction())
        }
    }
}

internal fun setupFatFrameworkTasks(project: Project) {
    project.tasks.withType<FatFrameworkTask>().configureEach {
        @Suppress("UNCHECKED_CAST")
        it.doLast(CopyAppleResourcesFromFrameworkToFatAction() as Action<Task>)
    }
}
