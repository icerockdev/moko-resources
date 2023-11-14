/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.configuration

import com.android.build.api.dsl.AndroidSourceSet
import dev.icerock.gradle.generator.MRGenerator
import dev.icerock.gradle.utils.capitalize
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.io.File

internal fun createSourceSet(kotlinSourceSet: KotlinSourceSet): MRGenerator.SourceSet {
    return object : MRGenerator.SourceSet {
        override val name: String
            get() = kotlinSourceSet.name

        override fun addSourceDir(directory: File) {
            kotlinSourceSet.kotlin.srcDir(directory)
        }

        override fun addResourcesDir(directory: File) {
            kotlinSourceSet.resources.srcDir(directory)
        }

        override fun addAssetsDir(directory: File) {
            // nothing
        }
    }
}

internal fun createSourceSet(
    androidSourceSet: AndroidSourceSet,
): MRGenerator.SourceSet {
    return object : MRGenerator.SourceSet {
        override val name: String
            get() = "android${androidSourceSet.name.capitalize()}"

        override fun addSourceDir(directory: File) {
            androidSourceSet.java.srcDirs(directory)
//                targets.configureEach { target ->
//                    target.compilations.configureEach { compilation ->
//                        val lazyDirectory = {
//                            directory.takeIf {
//                                compilation.kotlinSourceSets.any { compilationSourceSet ->
//                                    compilationSourceSet.isDependsOn(commonSourceSet)
//                                }
//                            }
//                        }
//                        compilation.defaultSourceSet.kotlin.srcDir(lazyDirectory)
//                    }
//                }
        }

        override fun addResourcesDir(directory: File) {
            androidSourceSet.res.srcDir(directory)
        }

        override fun addAssetsDir(directory: File) {
            androidSourceSet.assets.srcDir(directory)
        }
    }
}
