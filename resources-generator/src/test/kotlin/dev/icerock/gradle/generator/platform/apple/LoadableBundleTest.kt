/*
 * Copyright 2026 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.platform.apple

import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LoadableBundleTest {
    @Test
    fun `bundle directory uses public bundle identifier`() {
        val temporaryDirectory: File = createTempDirectory("loadable-bundle-test").toFile()
        try {
            val loadableBundle = LoadableBundle(
                directory = temporaryDirectory,
                developmentRegion = "en",
                identifier = "com.example.resources.main",
            )

            loadableBundle.write()

            assertEquals(
                "com.example.resources.main.bundle",
                loadableBundle.bundleDir.name,
            )
            assertTrue(loadableBundle.infoPListFile.isFile)
            assertTrue(
                loadableBundle.infoPListFile.readText()
                    .contains("<string>com.example.resources.main</string>")
            )
        } finally {
            temporaryDirectory.deleteRecursively()
        }
    }
}
