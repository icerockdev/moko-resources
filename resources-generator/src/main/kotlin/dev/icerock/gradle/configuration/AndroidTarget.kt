/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.configuration

//import dev.icerock.gradle.generator.MRGenerator
//import dev.icerock.gradle.generator.ResourceGeneratorFeature
//import dev.icerock.gradle.generator.android.AndroidMRGenerator

//private fun setAssetsDirsRefresh(project: Project) {
//    // without this code Android Gradle Plugin not copy assets to aar
//    project.tasks
//        .matching { it.name.startsWith("package") && it.name.endsWith("Assets") }
//        .configureEach { task ->
//            // for gradle optimizations we should use anonymous object
//            @Suppress("ObjectLiteralToLambda")
//            task.doFirst(object : Action<Task> {
//                override fun execute(t: Task) {
//                    val android: BaseExtension = project.extensions.getByType()
//                    val assets: AndroidSourceDirectorySet = android.mainSourceSet.assets
//                    assets.setSrcDirs(assets.srcDirs)
//                }
//            })
//        }
//}
