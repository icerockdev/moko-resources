/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.actions.apple

import dev.icerock.gradle.generator.Constants
import dev.icerock.gradle.generator.platform.apple.LoadableBundle
import dev.icerock.gradle.utils.unzipTo
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.Task
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile
import org.jetbrains.kotlin.konan.file.zipDirAs
import java.io.File
import java.util.Properties

internal class PackAppleResourcesToKLibAction(
    private val assetsDirectory: Provider<File>,
    private val baseLocalizationRegion: Provider<String>,
    private val bundleIdentifier: Provider<String>,
    private val resourcesGenerationDir: Provider<File>,
    private val iosMinimalDeploymentTarget: Provider<String>
) : Action<Task> {
    override fun execute(task: Task) {
        task as KotlinNativeCompile

        val assetsDirectory: File = assetsDirectory.get()
        val resourcesGenerationDir: File = resourcesGenerationDir.get()

        val resourcesExists: Boolean = listOf(
            assetsDirectory,
            resourcesGenerationDir
        ).any { dir ->
            dir.exists() && dir.walkTopDown().any { it.isFile }
        }

        if (!resourcesExists) {
            task.logger.info("Resources not found. Skip klib repack action.")
            return
        }

        val klibFile: File = task.outputFile.get()
        val repackDir = File(klibFile.parent, klibFile.nameWithoutExtension)
        val defaultDir = File(repackDir, "default")
        val resRepackDir = File(defaultDir, "resources")

        task.logger.info("Adding resources to klib file `{}`", klibFile)
        unzipTo(zipFile = klibFile, outputDirectory = repackDir)

        val manifestFile = File(defaultDir, "manifest")
        val manifest = Properties()
        manifest.load(manifestFile.inputStream())

        val uniqueName: String = manifest["unique_name"] as String

        val loadableBundle = LoadableBundle(
            directory = resRepackDir,
            bundleName = uniqueName,
            developmentRegion = baseLocalizationRegion.get(),
            identifier = bundleIdentifier.get()
        )

        loadableBundle.write()

        if (resourcesGenerationDir.exists()) {
            resourcesGenerationDir.copyRecursively(
                loadableBundle.resourcesDir,
                overwrite = true
            )
        }
        if (assetsDirectory.exists()) {
            assetsDirectory.copyRecursively(
                loadableBundle.resourcesDir,
                overwrite = true
            )
        }

        val rawAssetsDir = File(loadableBundle.resourcesDir, Constants.Apple.assetsDirectoryName)
        if (rawAssetsDir.exists()) {
            compileAppleAssets(rawAssetsDir, task)
        } else {
            task.logger.info("assets not found, compilation not required")
        }

        val repackKonan = org.jetbrains.kotlin.konan.file.File(repackDir.path)
        val klibKonan = org.jetbrains.kotlin.konan.file.File(klibFile.path)

        klibFile.delete()
        repackKonan.zipDirAs(klibKonan)

        repackDir.deleteRecursively()
    }

    private fun compileAppleAssets(
        rawAssetsDir: File,
        task: KotlinNativeCompile
    ) {
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
            task.logger.error("can't compile assets - $result")
            task.logger.info(input)
            task.logger.error(errors)
            throw GradleException("Assets compilation failed: $errors")
        } else {
            task.logger.info("assets compiled")
            rawAssetsDir.deleteRecursively()
        }
    }
}
