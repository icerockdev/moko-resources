/*
 * Copyright 2026 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.actions.apple

import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PopulateDummyFrameworkResourcesActionTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `creates requested dummy bundles with ownership markers`() {
        val framework = temporaryFolder.newFolder("shared.framework")

        populateDummyFrameworkBundles(
            frameworkDirectory = framework,
            bundleNames = setOf("module.main.bundle", "dependency.main.bundle"),
        )

        assertTrue(File(framework, "module.main.bundle/$DUMMY_BUNDLE_MARKER_FILE_NAME").isFile)
        assertTrue(File(framework, "dependency.main.bundle/$DUMMY_BUNDLE_MARKER_FILE_NAME").isFile)
    }

    @Test
    fun `removes only stale moko resource dummy bundles`() {
        val framework = temporaryFolder.newFolder("shared.framework")
        val staleBundle = createBundle(framework, "stale.main.bundle", withMarker = true)
        val realBundle = createBundle(framework, "real.main.bundle", withMarker = false)

        populateDummyFrameworkBundles(
            frameworkDirectory = framework,
            bundleNames = setOf("current.main.bundle"),
        )

        assertFalse(staleBundle.exists())
        assertTrue(realBundle.isDirectory)
        assertTrue(File(realBundle, "real-resource").isFile)
        assertTrue(File(framework, "current.main.bundle/$DUMMY_BUNDLE_MARKER_FILE_NAME").isFile)
    }

    private fun createBundle(
        framework: File,
        name: String,
        withMarker: Boolean,
    ): File = File(framework, name).apply {
        mkdirs()
        val fileName = if (withMarker) DUMMY_BUNDLE_MARKER_FILE_NAME else "real-resource"
        File(this, fileName).writeText("content")
    }
}
