/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.tasks.FatFrameworkTask

open class FatFrameworkWithResourcesTask : FatFrameworkTask() {

    @TaskAction
    protected fun copyBundle() {
        super.createFatFramework()

        frameworks.first().outputFile.listFiles()
            ?.asSequence()
            ?.filter { it.name.contains(".bundle") }
            ?.forEach { bundleFile ->
                project.copy {
                    it.from(bundleFile) { it.into(bundleFile.name) }
                    it.into(fatFrameworkDir)
                }
            }
    }
}
