/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.actions.js

import dev.icerock.gradle.utils.klibs
import org.gradle.api.Action
import org.gradle.api.logging.Logger
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import org.jetbrains.kotlin.library.KotlinLibraryLayout
import org.jetbrains.kotlin.library.impl.KotlinLibraryLayoutImpl
import java.io.File

internal class CopyResourcesToExecutableAction(
    private val resourcesGeneratedDir: Provider<File>,
    private val projectDir: Provider<File>
) : Action<Kotlin2JsCompile> {
    override fun execute(task: Kotlin2JsCompile) {
        val resourceDir = resourcesGeneratedDir.get()

        task.klibs.forEach { dependency ->
            copyResourcesFromLibraries(
                logger = task.logger,
                inputFile = dependency,
                outputDir = resourceDir
            )
        }

        generateWebpackConfig(resourceDir)
        generateKarmaConfig()
    }

    private fun generateWebpackConfig(resourcesOutput: File) {
        val webpackDir = File(projectDir.get(), "webpack.config.d")
        webpackDir.mkdirs()

        val webpackConfig = File(webpackDir, "moko-resources-generated.js")
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

    private fun generateKarmaConfig() {
        val webpackDir = File(projectDir.get(), "karma.config.d")
        webpackDir.mkdirs()

        val webpackTestConfig = File(webpackDir, "moko-resources-generated.js")
        val pattern = "`\${output.path}/**/*`"
        webpackTestConfig.writeText(
            // language=js
            """
                // workaround from https://github.com/ryanclark/karma-webpack/issues/498#issuecomment-790040818

                const output = {
                  path: require("os").tmpdir() + '/' + '_karma_webpack_' + Math.floor(Math.random() * 1000000),
                }

                const optimization = {
                     runtimeChunk: true
                }

                config.set(
                    {
                        webpack: {... createWebpackConfig(), output, optimization},
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
        outputDir: File,
        logger: Logger
    ) {
        if (inputFile.extension != "klib") return
        if (inputFile.exists().not()) return

        logger.info("copy resources from $inputFile into $outputDir")
        val klibKonan = org.jetbrains.kotlin.konan.file.File(inputFile.path)
        val klib = KotlinLibraryLayoutImpl(klib = klibKonan, component = "default")
        val layout: KotlinLibraryLayout = klib.extractingToTemp

        try {
            File(layout.resourcesDir.path, "moko-resources-js").copyRecursively(
                target = outputDir,
                overwrite = true
            )
        } catch (@Suppress("SwallowedException") exc: NoSuchFileException) {
            logger.info("resources in $inputFile not found")
        } catch (@Suppress("SwallowedException") exc: java.nio.file.NoSuchFileException) {
            logger.info("resources in $inputFile not found (empty lib)")
        }
    }
}