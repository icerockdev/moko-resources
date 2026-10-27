/*
 * Copyright 2026 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.actions.apple

import org.gradle.api.logging.Logging
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppleAssetCatalogCompilerTest {
    @Test
    fun `maps Apple targets to actool platforms`() {
        assertEquals(AppleActoolPlatform.IOS_DEVICE, actoolPlatform("ios_arm64"))
        assertEquals(AppleActoolPlatform.IOS_SIMULATOR, actoolPlatform("ios_simulator_arm64"))
        assertEquals(AppleActoolPlatform.IOS_SIMULATOR, actoolPlatform("ios_x64"))
        assertEquals(AppleActoolPlatform.MACOS, actoolPlatform("macos_arm64"))
        assertEquals(AppleActoolPlatform.MACOS, actoolPlatform("macos_x64"))
        assertEquals(AppleActoolPlatform.TVOS_DEVICE, actoolPlatform("tvos_arm64"))
        assertEquals(
            AppleActoolPlatform.TVOS_SIMULATOR,
            actoolPlatform("tvos_simulator_arm64"),
        )
        assertEquals(AppleActoolPlatform.WATCHOS_DEVICE, actoolPlatform("watchos_arm64"))
        assertEquals(
            AppleActoolPlatform.WATCHOS_SIMULATOR,
            actoolPlatform("watchos_simulator_arm64"),
        )
    }

    @Test
    fun `only iOS platforms require configured deployment target`() {
        assertTrue(AppleActoolPlatform.IOS_DEVICE.requiresMinimumDeploymentTarget)
        assertTrue(AppleActoolPlatform.IOS_SIMULATOR.requiresMinimumDeploymentTarget)
        assertTrue(
            AppleActoolPlatform.entries
                .filterNot {
                    it == AppleActoolPlatform.IOS_DEVICE ||
                        it == AppleActoolPlatform.IOS_SIMULATOR
                }
                .none(AppleActoolPlatform::requiresMinimumDeploymentTarget)
        )
    }

    @Test
    fun `keeps precompiled asset catalog without invoking actool`() {
        val temporaryDirectory: File = createTempDirectory("apple-assets-test").toFile()
        try {
            val bundleDirectory = File(temporaryDirectory, "legacy.bundle")
            val compiledAssetCatalog = File(bundleDirectory, "Contents/Resources/Assets.car")
            compiledAssetCatalog.parentFile.mkdirs()
            compiledAssetCatalog.writeText("precompiled")

            AppleAssetCatalogCompiler(
                logger = Logging.getLogger(AppleAssetCatalogCompilerTest::class.java),
                iosMinimalDeploymentTarget = "12.0",
            ).compile(
                bundleDirectory = bundleDirectory,
                targetName = "invalid_target_proves_actool_was_not_used",
            )

            assertTrue(compiledAssetCatalog.isFile)
            assertEquals("precompiled", compiledAssetCatalog.readText())
        } finally {
            temporaryDirectory.deleteRecursively()
        }
    }
}
