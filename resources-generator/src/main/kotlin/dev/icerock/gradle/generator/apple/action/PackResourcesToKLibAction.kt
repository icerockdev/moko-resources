/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.apple.action

import dev.icerock.gradle.generator.apple.LoadableBundle
import dev.icerock.gradle.utils.unzipTo
import org.gradle.api.Action
import org.gradle.api.Task
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile
import org.jetbrains.kotlin.konan.file.zipDirAs
import java.io.File
import java.util.Properties

internal class PackResourcesToKLibAction(
    private val baseLocalizationRegion: Provider<String>,
    private val bundleIdentifierProvider: Provider<String>,
    private val assetsDirectoryProvider: Provider<File>,
    private val resourcesGenerationDirProvider: Provider<File>,
) : Action<Task> {
    override fun execute(task: Task) {
        task as KotlinNativeCompile

        val klibFile = task.outputFile.get()
        val repackDir = File(klibFile.parent, klibFile.nameWithoutExtension)
        val defaultDir = File(repackDir, "default")
        val resRepackDir = File(defaultDir, "resources")
        val assetsDirectory: File = assetsDirectoryProvider.get()
        val resourcesGenerationDir: File = resourcesGenerationDirProvider.get()

        task.logger.info("Adding resources to klib file `{}`", klibFile)
        unzipTo(zipFile = klibFile, outputDirectory = repackDir)

        val manifestFile = File(defaultDir, "manifest")
        val manifest = Properties()
        manifest.load(manifestFile.inputStream())

        val uniqueName = manifest["unique_name"] as String

        val loadableBundle = LoadableBundle(
            directory = resRepackDir,
            bundleName = uniqueName,
            developmentRegion = baseLocalizationRegion.get(),
            identifier = bundleIdentifierProvider.get()
        )
        loadableBundle.write()

        val process: Process = Runtime.getRuntime().exec(
            "xcrun actool Assets.xcassets --compile . --platform iphoneos --minimum-deployment-target 9.0",
            emptyArray(),
            assetsDirectory.parentFile
        )
        val errors: String = process.errorStream.bufferedReader().readText()
        val input: String = process.inputStream.bufferedReader().readText()
        val result: Int = process.waitFor()
        if (result != 0) {
            task.logger.error("can't compile assets - $result")
            task.logger.info(input)
            task.logger.error(errors)
        } else {
            task.logger.info("assets compiled")
            assetsDirectory.deleteRecursively()
        }

        resourcesGenerationDir.copyRecursively(
            loadableBundle.resourcesDir,
            overwrite = true
        )

        val repackKonan = org.jetbrains.kotlin.konan.file.File(repackDir.path)
        val klibKonan = org.jetbrains.kotlin.konan.file.File(klibFile.path)

        klibFile.delete()
        repackKonan.zipDirAs(klibKonan)

        repackDir.deleteRecursively()
    }
}
