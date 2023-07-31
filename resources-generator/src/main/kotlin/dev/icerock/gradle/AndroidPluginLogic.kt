/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle

import com.android.build.api.dsl.AndroidSourceSet
import com.android.build.gradle.BaseExtension
import dev.icerock.gradle.generator.MRGenerator
import dev.icerock.gradle.generator.ResourceGeneratorFeature
import dev.icerock.gradle.generator.SourceInfo
import dev.icerock.gradle.generator.android.AndroidMRGenerator
import dev.icerock.gradle.utils.isDependsOn
import org.gradle.api.Action
import org.gradle.api.DomainObjectCollection
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSet
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

@Suppress("LongParameterList")
internal class AndroidPluginLogic(
    private val commonSourceSet: KotlinSourceSet,
    private val targets: DomainObjectCollection<KotlinAndroidTarget>,
    private val generatedDir: Provider<Directory>,
    private val mrSettings: MRGenerator.MRSettings,
    private val features: List<ResourceGeneratorFeature<out MRGenerator.Generator>>,
    private val sourceInfo: SourceInfo,
    private val project: Project,
) {
    fun setup() {
        val androidExtension: BaseExtension = project.extensions.getByType(BaseExtension::class)

        val androidMainSourceSet = androidExtension.sourceSets
            .getByName(SourceSet.MAIN_SOURCE_SET_NAME)

        sourceInfo.androidRClassPackageProvider = project.provider {
            val namespace: String? = androidExtension.namespace
            if (namespace != null) {
                namespace
            } else {
                val manifestFile = androidMainSourceSet.manifest.srcFile
                getAndroidPackage(manifestFile)
            }
        }
        val androidSourceSet: MRGenerator.SourceSet = createSourceSet(androidMainSourceSet)

        setAssetsDirsRefresh()

        AndroidMRGenerator(
            generatedDir = generatedDir,
            sourceSet = androidSourceSet,
            mrSettings = mrSettings,
            generators = features.map { it.createAndroidGenerator() }
        ).apply(project)
    }

    private fun setAssetsDirsRefresh() {
        // without this code Android Gradle Plugin not copy assets to aar
        project.tasks
            .matching { it.name.startsWith("package") && it.name.endsWith("Assets") }
            .configureEach { task ->
                // for gradle optimizations we should use anonymous object
                @Suppress("ObjectLiteralToLambda")
                task.doFirst(object : Action<Task> {
                    override fun execute(t: Task) {
                        val android = project.extensions.getByType<BaseExtension>()
                        val assets = android.sourceSets.getByName("main").assets
                        assets.setSrcDirs(assets.srcDirs)
                    }
                })
            }
    }

    private fun createSourceSet(
        androidSourceSet: AndroidSourceSet,
    ): MRGenerator.SourceSet {
        return object : MRGenerator.SourceSet {
            override val name: String
                get() = "android${androidSourceSet.name.capitalize()}"

            override fun addSourceDir(directory: Provider<Directory>) {
                targets.configureEach { target ->
                    target.compilations.configureEach { compilation ->
                        val lazyDirectory = {
                            directory.takeIf {
                                compilation.kotlinSourceSets.any { compilationSourceSet ->
                                    compilationSourceSet.isDependsOn(commonSourceSet)
                                }
                            }
                        }
                        compilation.defaultSourceSet.kotlin.srcDir(lazyDirectory)
                    }
                }
            }

            override fun addResourcesDir(directory: Provider<Directory>) {
                androidSourceSet.res.srcDir(directory)
            }

            override fun addAssetsDir(directory: Provider<Directory>) {
                androidSourceSet.assets.srcDir(directory)
            }
        }
    }

    private companion object {
        private fun getAndroidPackage(manifestFile: File): String {
            val dbFactory = DocumentBuilderFactory.newInstance()
            val dBuilder = dbFactory.newDocumentBuilder()
            val doc = dBuilder.parse(manifestFile)

            val manifestNodes = doc.getElementsByTagName("manifest")
            val manifest = manifestNodes.item(0)

            return manifest.attributes.getNamedItem("package").textContent
        }
    }
}
