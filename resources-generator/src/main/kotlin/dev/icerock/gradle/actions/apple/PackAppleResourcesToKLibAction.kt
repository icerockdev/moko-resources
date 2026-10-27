/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.actions.apple

import dev.icerock.gradle.generator.platform.apple.LoadableBundle
import dev.icerock.gradle.utils.unzipTo
import dev.icerock.gradle.utils.zipDirAs
import org.gradle.api.Action
import org.gradle.api.Task
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile
import java.io.File

internal class PackAppleResourcesToKLibAction(
    private val assetsDirectory: Provider<File>,
    private val baseLocalizationRegion: Provider<String>,
    private val bundleIdentifier: Provider<String>,
    private val resourcesGenerationDir: Provider<File>,
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

        if (klibFile.isDirectory) {
            task.logger.info("Adding resources to unpacked klib directory `{}`", klibFile)

            addResourcesToUnpackedKlib(
                klibDir = klibFile,
                resourcesGenerationDir = resourcesGenerationDir,
                assetsDirectory = assetsDirectory,
            )
        } else {
            task.logger.info("Adding resources to packed klib directory `{}`", klibFile)

            unzipTo(zipFile = klibFile, outputDirectory = repackDir)

            addResourcesToUnpackedKlib(
                klibDir = repackDir,
                resourcesGenerationDir = resourcesGenerationDir,
                assetsDirectory = assetsDirectory,
            )

            klibFile.delete()
            repackDir.zipDirAs(klibFile)

            repackDir.deleteRecursively()
        }
    }

    private fun addResourcesToUnpackedKlib(
        klibDir: File,
        resourcesGenerationDir: File,
        assetsDirectory: File,
    ) {
        assert(klibDir.isDirectory) { "should be used directory as KLib" }

        val defaultDir = File(klibDir, "default")
        val resRepackDir = File(defaultDir, "resources")

        val loadableBundle = LoadableBundle(
            directory = resRepackDir,
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
    }
}
