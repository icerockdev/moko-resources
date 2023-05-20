/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.js

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.generator.MRGenerator
import dev.icerock.gradle.utils.calculateResourcesHash
import dev.icerock.gradle.utils.dependsOnProcessResources
import dev.icerock.gradle.utils.klibs
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrCompilation
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrTarget
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import org.jetbrains.kotlin.library.impl.KotlinLibraryLayoutImpl
import java.io.File

class JsMRGenerator(
    generatedDir: File,
    sourceSet: SourceSet,
    mrSettings: MRSettings,
    generators: List<Generator>,
    private val compilation: KotlinJsIrCompilation
) : MRGenerator(
    generatedDir = generatedDir,
    sourceSet = sourceSet,
    mrSettings = mrSettings,
    generators = generators
) {
    private val flattenClassName: String get() = mrSettings.packageName.replace(".", "")

    override val resourcesGenerationDir: File
        get() = outputDir.resolve("$flattenClassName/res")

    override fun getMRClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun processMRClass(mrClass: TypeSpec.Builder) {
        mrClass.addProperty(
            PropertySpec.builder("contentHash", STRING, KModifier.PRIVATE)
                .initializer("%S", resourcesGenerationDir.calculateResourcesHash())
                .build()
        )

        val stringsLoaderInitializer = buildList {
            val stringsObjectLoader = mrClass
                .typeSpecs
                .find { it.name == "strings" }
                ?.propertySpecs
                ?.find { it.name == "stringsLoader" }

            val pluralsObjectLoader = mrClass
                .typeSpecs
                .find { it.name == "plurals" }
                ?.propertySpecs
                ?.find { it.name == "stringsLoader" }

            if (stringsObjectLoader != null) {
                add("strings.stringsLoader")
            }
            if (pluralsObjectLoader != null) {
                add("plurals.stringsLoader")
            }
        }.takeIf(List<*>::isNotEmpty)
            ?.joinToString(separator = " + ")

        if (stringsLoaderInitializer != null) {
            mrClass.addProperty(
                PropertySpec.builder(
                    "stringsLoader",
                    ClassName("dev.icerock.moko.resources.provider", "RemoteJsStringLoader"),
                ).initializer(stringsLoaderInitializer)
                    .build()
            )
        }
    }

    override fun apply(generationTask: Task, project: Project) {
        project.tasks.withType<Kotlin2JsCompile>().configureEach {
            it.dependsOn(generationTask)
        }
        setupKLibResources(generationTask)
        setupResources()

        // Declare task ':web-app:generateMRcommonMain' as an input of ':web-app:jsSourcesJar'.
        project.tasks.withType<Jar>().configureEach {
            it.dependsOn(generationTask)
        }

        dependsOnProcessResources(project, sourceSet, generationTask)
    }

    private fun setupKLibResources(generationTask: Task) {
        val compileTask: Kotlin2JsCompile = compilation.compileKotlinTask
        compileTask.dependsOn(generationTask)

        @Suppress("UNCHECKED_CAST")
        compileTask.doLast(CopyResourcesToKLibAction(resourcesGenerationDir) as Action<in Task>)
    }

    private fun setupResources() {
        val kotlinTarget = compilation.target as KotlinJsIrTarget

        kotlinTarget.compilations
            .map { it.compileKotlinTask }
            .forEach { compileTask ->
                @Suppress("UNCHECKED_CAST")
                compileTask.doLast(CopyResourcesToExecutableAction(resourcesGenerationDir) as Action<in Task>)
            }
    }

    class CopyResourcesToKLibAction(private val resourcesDir: File) : Action<Kotlin2JsCompile> {
        override fun execute(task: Kotlin2JsCompile) {
            val unpackedKLibDir: File = task.outputFileProperty.get()
            val defaultDir = File(unpackedKLibDir, "default")
            val resRepackDir = File(defaultDir, "resources")
            if (resRepackDir.exists().not()) return

            val resDir = File(resRepackDir, "moko-resources-js")
            resourcesDir.copyRecursively(
                resDir,
                overwrite = true
            )
        }
    }

    class CopyResourcesToExecutableAction(
        private val resourcesGeneratedDir: File
    ) : Action<Kotlin2JsCompile> {
        override fun execute(task: Kotlin2JsCompile) {
            val project: Project = task.project

            task.klibs.forEach { dependency ->
                copyResourcesFromLibraries(
                    inputFile = dependency,
                    project = project,
                    outputDir = resourcesGeneratedDir
                )
            }

            generateWebpackConfig(project, resourcesGeneratedDir)
            generateKarmaConfig(project)
        }

        private fun generateWebpackConfig(project: Project, resourcesOutput: File) {
            val webpackDir: File = File(project.projectDir, "webpack.config.d")
            webpackDir.mkdirs()

            val webpackConfig: File = File(webpackDir, "moko-resources-generated.js")
            val webpackResourcesDir: String = resourcesOutput.absolutePath
                .replace("\\", "\\\\")

            webpackConfig.writeText(
                // language=js
                """
// noinspection JSUnnecessarySemicolon
;(function(config) {
    const path = require('path');
    const MiniCssExtractPlugin = require('mini-css-extract-plugin');

    const mokoResourcePath = path.resolve("$webpackResourcesDir");

    config.module.rules.push(
        {
            test: /\.(.*)/,
            resource: [
                path.resolve(mokoResourcePath, "files"),
                path.resolve(mokoResourcePath, "images"),
                path.resolve(mokoResourcePath, "localization"),
            ],
            type: 'asset/resource'
        }
    );
    
    config.plugins.push(new MiniCssExtractPlugin())
    config.module.rules.push(
        {
            test: /\.css${'$'}/,
            resource: [
                path.resolve(mokoResourcePath, "fonts"),
            ],
            use: ['style-loader', 'css-loader']
        }
    )

    config.module.rules.push(
        {
            test: /\.(otf|ttf)?${'$'}/,
            resource: [
                path.resolve(mokoResourcePath, "fonts"),
            ],
            type: 'asset/resource',
        }
    )
    
    config.resolve.modules.push(mokoResourcePath);
})(config);
                """.trimIndent()
            )
        }

        private fun generateKarmaConfig(project: Project) {
            val webpackDir: File = File(project.projectDir, "karma.config.d")
            webpackDir.mkdirs()

            val webpackTestConfig: File = File(webpackDir, "moko-resources-generated.js")
            val pattern = "`\${output.path}/**/*`"
            webpackTestConfig.writeText(
                // language=js
                """
                // workaround from https://github.com/ryanclark/karma-webpack/issues/498#issuecomment-790040818

                const output = {
                  path: require("os").tmpdir() + '/' + '_karma_webpack_' + Math.floor(Math.random() * 1000000),
                }
                
                config.set(
                    {
                        webpack: {... createWebpackConfig(), output},
                        files: config.files.concat([{
                                pattern: $pattern,
                                watched: false,
                                included: false,
                            }]
                        )
                    }
                )
                """.trimIndent()
            )
        }

        private fun copyResourcesFromLibraries(
            inputFile: File,
            project: Project,
            outputDir: File
        ) {
            if (inputFile.extension != "klib") return
            if (inputFile.exists().not()) return

            project.logger.info("copy resources from $inputFile into $outputDir")
            val klibKonan = org.jetbrains.kotlin.konan.file.File(inputFile.path)
            val klib = KotlinLibraryLayoutImpl(klib = klibKonan, component = "default")
            val layout = klib.extractingToTemp

            try {
                File(layout.resourcesDir.path, "moko-resources-js").copyRecursively(
                    target = outputDir,
                    overwrite = true
                )
            } catch (@Suppress("SwallowedException") exc: kotlin.io.NoSuchFileException) {
                project.logger.info("resources in $inputFile not found")
            } catch (@Suppress("SwallowedException") exc: java.nio.file.NoSuchFileException) {
                project.logger.info("resources in $inputFile not found (empty lib)")
            }
        }
    }

    companion object {
        const val STRINGS_JSON_NAME = "stringsJson"
        const val PLURALS_JSON_NAME = "pluralsJson"

        const val SUPPORTED_LOCALES_PROPERTY_NAME = "supportedLocales"
        const val STRINGS_FALLBACK_FILE_URL_PROPERTY_NAME = "stringsFallbackFileUrl"
        const val PLURALS_FALLBACK_FILE_URL_PROPERTY_NAME = "stringsFallbackFileUrl"
        const val LOCALIZATION_DIR = "localization"
    }
}
