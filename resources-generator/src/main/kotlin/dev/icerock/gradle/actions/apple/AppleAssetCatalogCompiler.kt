/*
 * Copyright 2026 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.actions.apple

import dev.icerock.gradle.generator.Constants
import org.gradle.api.GradleException
import org.gradle.api.logging.Logger
import java.io.File

internal class AppleAssetCatalogCompiler(
    private val logger: Logger,
    private val iosMinimalDeploymentTarget: String,
) {
    fun compile(bundleDirectory: File, targetName: String) {
        val resourcesDirectory = File(bundleDirectory, "Contents/Resources")
        val assetCatalog = File(resourcesDirectory, Constants.Apple.assetsDirectoryName)
        if (!assetCatalog.isDirectory) return

        val platform: AppleActoolPlatform = actoolPlatform(targetName)
        val compiledAssetCatalog = File(resourcesDirectory, COMPILED_ASSET_CATALOG_NAME)

        compiledAssetCatalog.delete()

        val command: MutableList<String> = mutableListOf(
            "xcrun",
            "actool",
            assetCatalog.absolutePath,
            "--compile",
            resourcesDirectory.absolutePath,
            "--platform",
            platform.cliName,
        )
        if (platform.requiresMinimumDeploymentTarget) {
            command += listOf(
                "--minimum-deployment-target",
                iosMinimalDeploymentTarget,
            )
        }

        logger.info(
            "Compiling Apple asset catalog in {} for {}",
            bundleDirectory,
            platform.cliName,
        )
        val process: Process = ProcessBuilder(command)
            .redirectErrorStream(true)
            .start()
        val output: String = process.inputStream.bufferedReader().use { it.readText() }
        val exitCode: Int = process.waitFor()

        if (exitCode != 0) {
            throw GradleException(
                "Apple asset catalog compilation failed for $bundleDirectory " +
                    "with exit code $exitCode:\n$output"
            )
        }
        if (!compiledAssetCatalog.isFile) {
            throw GradleException(
                "Apple asset catalog compiler did not create $compiledAssetCatalog:\n$output"
            )
        }

        assetCatalog.deleteRecursively()
        logger.info("Apple asset catalog compiled in {}", bundleDirectory)
    }
}

internal enum class AppleActoolPlatform(
    val cliName: String,
    val requiresMinimumDeploymentTarget: Boolean = false,
) {
    IOS_DEVICE(
        cliName = "iphoneos",
        requiresMinimumDeploymentTarget = true,
    ),
    IOS_SIMULATOR(
        cliName = "iphonesimulator",
        requiresMinimumDeploymentTarget = true,
    ),
    MACOS(cliName = "macosx"),
    TVOS_DEVICE(cliName = "appletvos"),
    TVOS_SIMULATOR(cliName = "appletvsimulator"),
    WATCHOS_DEVICE(cliName = "watchos"),
    WATCHOS_SIMULATOR(cliName = "watchsimulator"),
}

internal fun actoolPlatform(targetName: String): AppleActoolPlatform {
    return when (targetName) {
        "ios_arm64" -> AppleActoolPlatform.IOS_DEVICE
        "ios_simulator_arm64", "ios_x64" -> AppleActoolPlatform.IOS_SIMULATOR
        "macos_arm64", "macos_x64" -> AppleActoolPlatform.MACOS
        "tvos_arm64" -> AppleActoolPlatform.TVOS_DEVICE
        "tvos_simulator_arm64", "tvos_x64" -> AppleActoolPlatform.TVOS_SIMULATOR
        "watchos_arm32", "watchos_arm64", "watchos_device_arm64" -> {
            AppleActoolPlatform.WATCHOS_DEVICE
        }
        "watchos_simulator_arm64", "watchos_x64" -> AppleActoolPlatform.WATCHOS_SIMULATOR
        else -> throw GradleException("Kotlin/Native target '$targetName' is not an Apple target")
    }
}

private const val COMPILED_ASSET_CATALOG_NAME = "Assets.car"
