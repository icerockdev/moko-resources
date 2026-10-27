/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.actions.apple

import dev.icerock.gradle.data.getAppleBundlesFromKlibSources
import dev.icerock.gradle.utils.klibs
import org.gradle.api.Action
import org.gradle.api.Task
import org.gradle.api.logging.Logger
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
import java.io.File

internal abstract class CopyResourcesFromKLibsAction(
    private val iosMinimalDeploymentTarget: Provider<String>,
) : Action<Task> {

    protected fun copyResourcesFromLibraries(
        linkTask: KotlinNativeLink,
        outputDir: File
    ) {
        val logger: Logger = linkTask.logger
        val assetCatalogCompiler = AppleAssetCatalogCompiler(
            logger = logger,
            iosMinimalDeploymentTarget = iosMinimalDeploymentTarget.get(),
        )

        linkTask.klibs
            .onEach { logger.debug("found klib dependency {}", it) }
            .let { getAppleBundlesFromKlibSources(sourceFiles = it, logger = logger) }
            .forEach { bundle ->
                val destinationBundle = File(outputDir, bundle.name)
                logger.info("copy $bundle to $destinationBundle")
                destinationBundle.deleteRecursively()
                bundle.copyRecursively(destinationBundle, overwrite = false)
                assetCatalogCompiler.compile(
                    bundleDirectory = destinationBundle,
                    targetName = linkTask.target,
                )
            }
    }
}
