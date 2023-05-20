/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.apple

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.MultiplatformResourcesPluginExtension
import dev.icerock.gradle.generator.MRGenerator
import dev.icerock.gradle.generator.apple.action.CopyResourcesFromKLibsToExecutableAction
import dev.icerock.gradle.generator.apple.action.CopyResourcesFromKLibsToFrameworkAction
import dev.icerock.gradle.generator.apple.action.PackResourcesToKLibAction
import dev.icerock.gradle.tasks.CopyExecutableResourcesToApp
import dev.icerock.gradle.tasks.CopyFrameworkResourcesToAppEntryPointTask
import dev.icerock.gradle.tasks.CopyFrameworkResourcesToAppTask
import dev.icerock.gradle.utils.calculateResourcesHash
import dev.icerock.gradle.utils.dependsOnProcessResources
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.maybeCreate
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.cocoapods.CocoapodsExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.AbstractExecutable
import org.jetbrains.kotlin.gradle.plugin.mpp.AbstractKotlinNativeCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.TestExecutable
import org.jetbrains.kotlin.gradle.tasks.FatFrameworkTask
import org.jetbrains.kotlin.gradle.tasks.FrameworkDescriptor
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
import java.io.File
import kotlin.reflect.full.memberProperties

@Suppress("TooManyFunctions")
class AppleMRGenerator(
    generatedDir: File,
    sourceSet: SourceSet,
    mrSettings: MRSettings,
    generators: List<Generator>,
    private val compilation: AbstractKotlinNativeCompilation,
    private val baseLocalizationRegion: String
) : MRGenerator(
    generatedDir = generatedDir,
    sourceSet = sourceSet,
    mrSettings = mrSettings,
    generators = generators
) {
    private val bundleClassName =
        ClassName("platform.Foundation", "NSBundle")
    private val bundleIdentifier = "${mrSettings.packageName}.MR"

    private var assetsDirectory: File? = null

    override fun getMRClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun processMRClass(mrClass: TypeSpec.Builder) {
        super.processMRClass(mrClass)

        mrClass.addProperty(
            PropertySpec.builder(
                BUNDLE_PROPERTY_NAME,
                bundleClassName,
                KModifier.PRIVATE
            )
                .delegate(CodeBlock.of("lazy { NSBundle.loadableBundle(\"$bundleIdentifier\") }"))
                .build()
        )

        mrClass.addProperty(
            PropertySpec.builder("contentHash", STRING, KModifier.PRIVATE)
                .initializer("%S", resourcesGenerationDir.calculateResourcesHash())
                .build()
        )
    }

    override fun getImports(): List<ClassName> = listOf(
        bundleClassName,
        ClassName("dev.icerock.moko.resources.utils", "loadableBundle")
    )

    override fun apply(generationTask: Task, project: Project) {
        createCopyResourcesToAppTask(project)
        setupKLibResources(generationTask)
        setupFrameworkResources()
        setupTestsResources()
        setupFatFrameworkTasks()

        dependsOnProcessResources(project, sourceSet, generationTask)
    }

    override fun beforeMRGeneration() {
        assetsDirectory = File(resourcesGenerationDir, ASSETS_DIR_NAME).apply {
            mkdirs()
        }
    }

    private fun setupKLibResources(generationTask: Task) {
        val compileTask: KotlinNativeCompile = compilation.compileKotlinTask
        compileTask.dependsOn(generationTask)

        // tasks like compileIosMainKotlinMetadata when only one target enabled
        generationTask.project.tasks
            .withType<KotlinCommonCompile>()
            .matching { it.name.contains(sourceSet.name, ignoreCase = true) }
            .configureEach { it.dependsOn(generationTask) }

        compileTask.doLast(
            PackResourcesToKLibAction(
                baseLocalizationRegion = baseLocalizationRegion,
                bundleIdentifier = bundleIdentifier,
                assetsDirectory = assetsDirectory,
                resourcesGenerationDir = resourcesGenerationDir,
            )
        )
    }

    private fun setupFrameworkResources() {
        val kotlinNativeTarget = compilation.target as KotlinNativeTarget
        val project = kotlinNativeTarget.project

        kotlinNativeTarget.binaries
            .matching { it is Framework && it.compilation == compilation }
            .configureEach { binary ->
                val framework = binary as Framework

                val linkTask = framework.linkTask

                linkTask.doLast(CopyResourcesFromKLibsToFrameworkAction())

                if (framework.isStatic) {
                    val resourcesExtension =
                        project.extensions.getByType<MultiplatformResourcesPluginExtension>()
                    if (resourcesExtension.disableStaticFrameworkWarning.not()) {
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

    private fun createCopyResourcesToAppTask(project: Project) {
        project.tasks.withType<KotlinNativeLink>()
            .matching { it.binary is AbstractExecutable }
            .all { linkTask ->
                val copyTaskName = linkTask.name.replace("link", "copyResources")

                project.tasks
                    .maybeCreate(copyTaskName, CopyExecutableResourcesToApp::class)
                    .apply {
                        this.linkTask = linkTask
                        this.dependsOn(linkTask)
                    }
            }
    }

    private fun createCopyFrameworkResourcesTask(linkTask: KotlinNativeLink) {
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

    private fun setupTestsResources() {
        val kotlinNativeTarget = compilation.target as KotlinNativeTarget

        kotlinNativeTarget.binaries
            .matching { it is TestExecutable && it.compilation.associateWith.contains(compilation) }
            .configureEach {
                val executable = it as TestExecutable
                val linkTask = executable.linkTask

                linkTask.doLast(CopyResourcesFromKLibsToExecutableAction())
            }
    }

    private fun setupFatFrameworkTasks() {
        val kotlinNativeTarget = compilation.target as KotlinNativeTarget
        val project = kotlinNativeTarget.project

        @Suppress("ObjectLiteralToLambda")
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

        project.tasks.withType(FatFrameworkTask::class)
            .configureEach { it.doLast(fatAction) }
    }

    companion object {
        const val BUNDLE_PROPERTY_NAME = "bundle"
        const val ASSETS_DIR_NAME = "Assets.xcassets"
    }
}
