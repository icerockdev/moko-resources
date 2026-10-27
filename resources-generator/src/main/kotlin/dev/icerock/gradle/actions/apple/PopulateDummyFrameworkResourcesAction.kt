/*
 * Copyright 2026 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.actions.apple

import dev.icerock.gradle.data.getAppleBundlesFromKlibSources
import org.gradle.api.Action
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.tasks.DummyFrameworkTask
import java.io.File

internal class PopulateDummyFrameworkResourcesAction(
    private val bundleIdentifiers: Provider<Set<String>>,
    private val klibs: FileCollection,
) : Action<Task> {

    override fun execute(task: Task) {
        check(task is DummyFrameworkTask)

        val bundleNames: Set<String> = buildSet {
            bundleIdentifiers.get().mapTo(this) { "$it.bundle" }
            getAppleBundlesFromKlibSources(
                sourceFiles = klibs.filter(File::exists),
                logger = task.logger,
            ).mapTo(this) { it.name }
        }

        populateDummyFrameworkBundles(
            frameworkDirectory = task.outputFramework.get().asFile,
            bundleNames = bundleNames,
        )
    }
}

internal fun populateDummyFrameworkBundles(
    frameworkDirectory: File,
    bundleNames: Set<String>,
) {
    frameworkDirectory.listFiles()
        ?.filter { bundleDirectory ->
            bundleDirectory.isDirectory &&
                bundleDirectory.extension == "bundle" &&
                File(bundleDirectory, DUMMY_BUNDLE_MARKER_FILE_NAME).isFile &&
                bundleDirectory.name !in bundleNames
        }
        ?.forEach(File::deleteRecursively)

    bundleNames.forEach { bundleName ->
        val bundleDirectory = File(frameworkDirectory, bundleName)
        bundleDirectory.mkdirs()
        File(bundleDirectory, DUMMY_BUNDLE_MARKER_FILE_NAME)
            .writeText(DUMMY_BUNDLE_MARKER_CONTENT)
    }
}

internal const val DUMMY_BUNDLE_MARKER_FILE_NAME = "moko-resources-dummy-bundle"
private const val DUMMY_BUNDLE_MARKER_CONTENT =
    "This dummy resource bundle is managed by moko-resources."
