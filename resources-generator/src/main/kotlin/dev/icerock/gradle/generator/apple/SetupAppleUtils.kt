package dev.icerock.gradle.generator.apple

import dev.icerock.gradle.MultiplatformResourcesPluginExtension
import dev.icerock.gradle.generator.apple.action.CopyResourcesFromKLibsToExecutableAction
import dev.icerock.gradle.generator.apple.action.CopyResourcesFromKLibsToFrameworkAction
import dev.icerock.gradle.generator.apple.action.PackResourcesToKLibAction
import dev.icerock.gradle.tasks.CopyFrameworkResourcesToAppEntryPointTask
import dev.icerock.gradle.tasks.CopyFrameworkResourcesToAppTask
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.cocoapods.CocoapodsExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.TestExecutable
import org.jetbrains.kotlin.gradle.tasks.FatFrameworkTask
import org.jetbrains.kotlin.gradle.tasks.FrameworkDescriptor
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
import java.io.File
import kotlin.reflect.full.memberProperties

fun setupAppleKLibResources(
    compileTask: KotlinNativeCompile,
    assetsDirectory: Provider<File>,
    resourcesGenerationDir: Provider<File>,
    iosLocalizationRegion: Provider<String>,
    resourcePackageName: Provider<String>,
) {
    compileTask.doLast {
        PackResourcesToKLibAction(
            baseLocalizationRegion = iosLocalizationRegion,
            resourcePackageName = resourcePackageName,
            assetsDirectory = assetsDirectory,
            resourcesGenerationDir = resourcesGenerationDir,
        )
    }

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

fun setupFrameworkResources(compilation: KotlinNativeCompilation) {
    compilation.target.binaries.withType<Framework>().configureEach { framework ->
        framework.linkTaskProvider.configure { linkTask ->
            linkTask.doLast(CopyResourcesFromKLibsToFrameworkAction())

            if (framework.isStatic) {
                val project: Project = linkTask.project
                val resourcesExtension: MultiplatformResourcesPluginExtension =
                    project.extensions.getByType()
                if (resourcesExtension.staticFrameworkWarningEnabled.get()) {
                    project.logger.warn(
                        """
$linkTask produces static framework, Xcode should have Build Phase with copyFrameworkResourcesToApp gradle task call. Please read readme on https://github.com/icerockdev/moko-resources
"""
                    )
                }
                createCopyFrameworkResourcesTask(linkTask)
            }
        }
    }
}

fun createCopyFrameworkResourcesTask(linkTask: KotlinNativeLink) {
    val framework = linkTask.binary as Framework
    val project = linkTask.project
    val taskName = linkTask.name.replace("link", "copyResources")

    val copyTask = project.tasks.create(taskName, CopyFrameworkResourcesToAppTask::class.java) {
        it.framework = framework
    }
    copyTask.dependsOn(linkTask)

    val xcodeTask = project.tasks.maybeCreate(
        "copyFrameworkResourcesToApp",
        CopyFrameworkResourcesToAppEntryPointTask::class.java
    )
    val multiplatformExtension = project.extensions.getByType<KotlinMultiplatformExtension>()
    xcodeTask.configurationMapper = (multiplatformExtension as? ExtensionAware)?.extensions
        ?.findByType<CocoapodsExtension>()
        ?.xcodeConfigurationToNativeBuildType
        ?: emptyMap()

    if (framework.target.konanTarget == xcodeTask.konanTarget &&
        framework.buildType.getName() == xcodeTask.configuration?.lowercase()
    ) {
        xcodeTask.dependsOn(copyTask)
    }
}

fun setupTestsResources(compilation: KotlinNativeCompilation) {
    compilation.target.binaries.withType<TestExecutable>().configureEach {executable ->
        executable.linkTaskProvider.configure { link ->
            link.doLast(CopyResourcesFromKLibsToExecutableAction())
        }
    }
}

fun setupFatFrameworkTasks(compilation: KotlinNativeCompilation) {
    val project = compilation.project

    val fatAction: Action<Task> = object : Action<Task> {
        override fun execute(task: Task) {
            val fatTask: FatFrameworkTask = task as FatFrameworkTask

            // compatibility of this api was changed
            // from 1.6.10 to 1.6.20, so reflection was
            // used here.
            val fatFrameworkDir: File = FatFrameworkTask::class
                .memberProperties
                .run {
                    find { it.name == "fatFrameworkDir" }
                        ?: find { it.name == "fatFramework" }
                }?.invoke(fatTask) as File

            val frameworkFile = when (val any: Any = fatTask.frameworks.first()) {
                is Framework -> any.outputFile
                is FrameworkDescriptor -> any.files.rootDir
                else -> error("Unsupported type of $any")
            }

            executeWithFramework(fatFrameworkDir, frameworkFile)
        }

        private fun executeWithFramework(
            fatFrameworkDir: File,
            frameworkFile: File,
        ) = frameworkFile
            .listFiles()
            ?.asSequence()
            ?.filter { it.name.contains(".bundle") }
            ?.forEach { bundleFile ->
                project.copy {
                    it.from(bundleFile)
                    it.into(File(fatFrameworkDir, bundleFile.name))
                }
            }
    }

    project.tasks.withType<FatFrameworkTask>().configureEach {
        it.doLast(fatAction)
    }
}