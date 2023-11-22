/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.configuration

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.AndroidSourceDirectorySet
import com.android.build.gradle.api.AndroidSourceSet
import dev.icerock.gradle.generator.MRGenerator
import dev.icerock.gradle.generator.ResourceGeneratorFeature
import dev.icerock.gradle.generator.android.AndroidMRGenerator
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSet
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

internal fun configureAndroidTargetGenerator(
    target: KotlinTarget,
    settings: MRGenerator.Settings,
    features: List<ResourceGeneratorFeature<out MRGenerator.Generator>>
) {
    val project: Project = target.project

    listOf(
        "com.android.library",
        "com.android.application"
    ).forEach { id ->
        project.plugins.withId(id) {
            setupAndroidGenerator(
                settings = settings,
                features = features,
                project = project
            )
        }
    }
}

internal fun Project.getAndroidRClassPackage(): Provider<String> {
    return provider {
        val androidExt: BaseExtension = project.extensions.getByType()
        androidExt.namespace ?: getAndroidPackage(androidExt.mainSourceSet.manifest.srcFile)
    }
}

@Suppress("LongParameterList")
private fun setupAndroidGenerator(
    project: Project,
    settings: MRGenerator.Settings,
    features: List<ResourceGeneratorFeature<out MRGenerator.Generator>>,
) {
    setAssetsDirsRefresh(project)

    AndroidMRGenerator(
        settings = settings,
        generators = features.map { it.createAndroidGenerator() },
    ).apply(project)
}

private fun setAssetsDirsRefresh(project: Project) {
    // without this code Android Gradle Plugin not copy assets to aar
    project.tasks
        .matching { it.name.startsWith("package") && it.name.endsWith("Assets") }
        .configureEach { task ->
            // for gradle optimizations we should use anonymous object
            @Suppress("ObjectLiteralToLambda")
            task.doFirst(object : Action<Task> {
                override fun execute(t: Task) {
                    val android: BaseExtension = project.extensions.getByType()
                    val assets: AndroidSourceDirectorySet = android.mainSourceSet.assets
                    assets.setSrcDirs(assets.srcDirs)
                }
            })
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

private val BaseExtension.mainSourceSet: AndroidSourceSet
    get() = this.sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)
