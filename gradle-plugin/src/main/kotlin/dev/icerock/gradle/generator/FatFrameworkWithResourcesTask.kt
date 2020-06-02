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
                    from(bundleFile) {
                        into(bundleFile.name)
                    }
                    into(fatFrameworkDir)
                }
            }
    }
}
