/*
 * Copyright 2025 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.utils

import com.android.build.gradle.BaseExtension
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.kotlin.dsl.findByType
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Resolves the Android package name (namespace) for standard Android application
 * and library modules.
 *
 * This function targets projects using the 'com.android.application' or
 * 'com.android.library' plugins. It follows a two-step resolution strategy:
 * 1. **Modern AGP (7.0+):** Attempts to read the `namespace` property directly from
 * the `android { ... }` DSL.
 * 2. **Legacy Fallback:** If the namespace is not set, it attempts to parse the
 * `package` attribute from the `AndroidManifest.xml` file.
 *
 * @param project The Gradle project instance to inspect.
 * @return
 * - The resolved namespace [String] if found.
 * - `"BaseExtension Android not found"` if the standard Android 'android' extension is missing.
 * - `null` if the namespace is not defined and manifest parsing fails.
 */
internal fun getAndroidTargetRClassPackage(project: Project): String? {
    val androidBaseExtension: BaseExtension = project.extensions.findByType<BaseExtension>()
        ?: return "BaseExtension Android not found"

    // Use namespace or parse it from the manifest (AGP < 7.x fallback)
    return androidBaseExtension.namespace ?: getAndroidPackage(
        manifestFile = project.file(project.file(androidBaseExtension.newMainSourceSet.manifest))
    )
}

/**
 * Parses the AndroidManifest.xml file to extract the 'package' attribute.
 *
 * This serves as a fallback mechanism for older Android projects (AGP < 7.0)
 * or projects that have not yet migrated to the modern `namespace` DSL property.
 * It uses standard XML DOM parsing to locate the root `<manifest>` node and
 * its `package` attribute.
 *
 * @param manifestFile The [File] object pointing to the AndroidManifest.xml.
 * @return The package name defined in the manifest, or an empty string/error
 * signal if the file does not exist or the attribute is missing.
 */
private fun getAndroidPackage(manifestFile: File): String {
    // Check if the file exists to avoid FileNotFoundException
    if (!manifestFile.exists()) {
        return "Manifest not found at ${manifestFile.absolutePath}"
    }

    val dbFactory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
    val dBuilder: DocumentBuilder = dbFactory.newDocumentBuilder()
    val doc: Document = dBuilder.parse(manifestFile)

    val manifestNodes: NodeList = doc.getElementsByTagName("manifest")
    val manifest: Node = manifestNodes.item(0)

    return manifest.attributes.getNamedItem("package").textContent
}

private val BaseExtension.newMainSourceSet: com.android.build.api.dsl.AndroidSourceSet
    get() = this.sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)
