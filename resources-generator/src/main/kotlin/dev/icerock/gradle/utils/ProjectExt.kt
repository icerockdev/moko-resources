/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.utils

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.AndroidSourceSet
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSet
import org.gradle.kotlin.dsl.findByType
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

internal fun Project.getAndroidRClassPackage(): Provider<String> {
    return provider {
        // before call android specific classes we should ensure that android plugin in classpath at all
        // it's required to support gradle projects without android target
        val isAndroidEnabled = listOf(
            "com.android.library",
            "com.android.application"
        ).any { project.plugins.findPlugin(it) != null }
        if (!isAndroidEnabled) return@provider null

        val androidExt: BaseExtension = project.extensions.findByType()
            ?: return@provider null
        androidExt.namespace ?: getAndroidPackage(androidExt.mainSourceSet.manifest.srcFile)
    }
}

private fun getAndroidPackage(manifestFile: File): String {
    val dbFactory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
    val dBuilder: DocumentBuilder = dbFactory.newDocumentBuilder()
    val doc: Document = dBuilder.parse(manifestFile)

    val manifestNodes: NodeList = doc.getElementsByTagName("manifest")
    val manifest: Node = manifestNodes.item(0)

    return manifest.attributes.getNamedItem("package").textContent
}

internal val BaseExtension.mainSourceSet: AndroidSourceSet
    get() = this.sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)
