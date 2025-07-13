/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.tasks

import dev.icerock.gradle.generator.Constants
import dev.icerock.gradle.generator.platform.apple.LoadableBundle
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class PackAppleResourcesBundleTask : DefaultTask() {
    init {
        group = "moko-resources"
    }

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:InputDirectory
    abstract val assetsDirectory: DirectoryProperty

    @get:InputDirectory
    abstract val resourcesGenerationDir: DirectoryProperty

    @get:Input
    abstract val baseLocalizationRegion: Property<String>

    @get:Input
    abstract val bundleIdentifier: Property<String>

    @get:Input
    abstract val iosMinimalDeploymentTarget: Property<String>

    @get:Input
    abstract val bundleName: Property<String>

    @TaskAction
    fun packResources() {
        val assetsDirectory: File = assetsDirectory.get().asFile
        val resourcesGenerationDir: File = resourcesGenerationDir.get().asFile

        val resourcesExists: Boolean = listOf(
            assetsDirectory,
            resourcesGenerationDir
        ).any { dir ->
            dir.exists() && dir.walkTopDown().any { it.isFile }
        }

        if (!resourcesExists) {
            logger.info("Resources not found. Skip klib repack action.")
            return
        }

        writeBundle(
            outputDirectory = outputDirectory.get().asFile,
            bundleName = bundleName.get()
        )
    }

    private fun writeBundle(
        outputDirectory: File,
        bundleName: String
    ) {
        val loadableBundle = LoadableBundle(
            directory = outputDirectory,
            bundleName = bundleName,
            developmentRegion = baseLocalizationRegion.get(),
            identifier = bundleIdentifier.get()
        )

        loadableBundle.write()

        val resDir: File = resourcesGenerationDir.get().asFile
        if (resDir.exists()) {
            resDir.copyRecursively(
                loadableBundle.resourcesDir,
                overwrite = true
            )
        }
        val assetsDir: File = assetsDirectory.get().asFile
        if (assetsDir.exists()) {
            assetsDir.copyRecursively(
                loadableBundle.resourcesDir,
                overwrite = true
            )
        }

        val rawAssetsDir = File(loadableBundle.resourcesDir, Constants.Apple.assetsDirectoryName)
        if (rawAssetsDir.exists()) {
            compileAppleAssets(rawAssetsDir)
        } else {
            logger.info("assets not found, compilation not required")
        }
    }

    private fun compileAppleAssets(rawAssetsDir: File) {
        val process: Process = Runtime.getRuntime().exec(
            buildString {
                append("xcrun actool ")
                append(rawAssetsDir.name)
                append(" --compile . --platform iphoneos --minimum-deployment-target ")
                append(iosMinimalDeploymentTarget.get())
            },
            emptyArray(),
            rawAssetsDir.parentFile
        )
        val errors: String = process.errorStream.bufferedReader().readText()
        val input: String = process.inputStream.bufferedReader().readText()
        val result: Int = process.waitFor()
        if (result != 0) {
            logger.error("can't compile assets - $result")
            logger.error(input)
            logger.error(errors)
            throw GradleException("Assets compilation failed: $errors")
        } else {
            logger.info("assets compiled")
            rawAssetsDir.deleteRecursively()
        }
    }
}
