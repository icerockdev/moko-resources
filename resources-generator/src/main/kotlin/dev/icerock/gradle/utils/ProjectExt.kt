/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.utils

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import com.android.build.api.variant.AndroidComponents
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.AndroidSourceSet
import dev.icerock.gradle.generator.platform.android.androidPlugins
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSet
import org.gradle.kotlin.dsl.findByType
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
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
        val isAndroidEnabled = androidPlugins().any { project.plugins.findPlugin(it) != null }
        if (!isAndroidEnabled) return@provider "android not enabled"
        // TODO ADD IF ELSE
        val newAndroidExtension: KotlinProjectExtension? =
            project.extensions.findByType()
        project.logger.warn("newAndroidExtension name=${newAndroidExtension}")
        val newExtension: KotlinMultiplatformAndroidLibraryExtension? =
            newAndroidExtension?.extensions?.findByType()
        project.logger.warn("newAndroidExtension name=${newExtension?.namespace}")
        if(newExtension!=null){
            return@provider newExtension.namespace
        }
        val androidExt: BaseExtension = project.extensions.findByType()
            ?: return@provider "androidExt not found"

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
