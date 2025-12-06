/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.utils

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.AndroidSourceSet
import dev.icerock.gradle.generator.platform.android.androidPlugins
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSet
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Resolves the Android R-class package name (namespace) for the current project.
 *
 * This function determines the package name used for generated Android R-classes
 * (Resource classes). It prioritizes the modern Kotlin Multiplatform (KMP)
 * Android Library Extension namespace, and falls back to the deprecated BaseExtension
 * and manifest file parsing if necessary.
 *
 * The evaluation is performed lazily inside a [Provider].
 *
 * @return A [Provider] that supplies the R-class package name as a [String].
 * The provider returns special error/signal strings if configuration fails or is not applicable:
 * - `"android not enabled"`: If none of the known Android plugins are applied to the project.
 * - `"BaseExtension Android not found"`: If Android plugins are detected but the
 * necessary [BaseExtension] (like `android` extension) cannot be located.
 * - The actual package name (e.g., "com.example.app"): The resolved namespace.
 */
internal fun Project.getAndroidRClassPackage(): Provider<String> {
    return provider {
        // before call android specific classes we should ensure that android plugin in classpath at all
        // it's required to support gradle projects without android target
        val isAndroidEnabled = androidPlugins().any { project.plugins.findPlugin(it) != null }
        if (!isAndroidEnabled) {
            return@provider "android not enabled"
        }

        val kotlinProjectExtension: KotlinProjectExtension? = project
            .extensions.findByType<KotlinProjectExtension>()
        val androidLibraryExtension: KotlinMultiplatformAndroidLibraryExtension? =
            kotlinProjectExtension?.extensions?.findByType<KotlinMultiplatformAndroidLibraryExtension>()

        // 1. Check for modern KMP Android Library Extension
        if (androidLibraryExtension != null) {
            return@provider androidLibraryExtension.namespace
        }

        // 2. Fallback to deprecated BaseExtension (e.g., Android Gradle Plugin 7.x/8.x)
        val androidBaseExtension: BaseExtension = project.extensions.findByType<BaseExtension>()
            ?: return@provider "BaseExtension Android not found"

        // 3. Use namespace or parse it from the manifest (AGP < 7.x fallback)
        androidBaseExtension.namespace ?: getAndroidPackage(
            manifestFile = androidBaseExtension.mainSourceSet.manifest.srcFile
        )
    }
}

/**
 * Enables Android resources support for all [KotlinMultiplatformAndroidLibraryTarget] targets
 * in this project.
 *
 * This function does **not** track plugin application and assumes that
 * the caller invokes it only after the `com.android.kotlin.multiplatform.library` plugin
 * has already been applied.
 *
 * It configures each Android library target to have `androidResources.enable = true`,
 * which allows the module to use Android resource folders (`res/`, `assets/`, etc.)
 * in the Kotlin Multiplatform Android source sets.
 */
internal fun Project.enableAndroidResources() {
    val kmpExtension = project.extensions.getByType(KotlinMultiplatformExtension::class.java)

    kmpExtension.targets.withType<KotlinMultiplatformAndroidLibraryTarget>()
        .configureEach { target ->
            target.androidResources.enable = true
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
