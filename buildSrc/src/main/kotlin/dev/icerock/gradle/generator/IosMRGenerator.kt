/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.*
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Copy
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
import java.io.File

class IosMRGenerator(
    generatedDir: File,
    sourceSet: KotlinSourceSet,
    mrClassPackage: String,
    generators: List<Generator>
) : MRGenerator(
    generatedDir = generatedDir,
    sourceSet = sourceSet,
    mrClassPackage = mrClassPackage,
    generators = generators
) {
    private val bundleClassName =
        ClassName("platform.Foundation", "NSBundle")

    override fun getMRClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun processMRClass(mrClass: TypeSpec.Builder) {
        super.processMRClass(mrClass)

        mrClass.addProperty(
            PropertySpec.builder(
                BUNDLE_PROPERTY_NAME,
                bundleClassName,
                KModifier.PRIVATE
            )
                .initializer(CodeBlock.of("NSBundle.bundleForClass(object_getClass(this)!!)"))
                .build()
        )
    }

    override fun getImports(): List<ClassName> = listOf(
        bundleClassName,
        ClassName("platform.objc", "object_getClass")
    )

    override fun apply(generationTask: Task, project: Project) {
        val linkTasks = project.tasks
            .mapNotNull { it as? KotlinNativeLink }
            .filter { it.binary is Framework }
            .filter { it.compilation.kotlinSourceSets.contains(sourceSet) }

        linkTasks.forEach { linkTask ->
            linkTask.dependsOn(generationTask)

            val framework = linkTask.binary as? Framework ?: return@forEach

            val pack =
                project.tasks.create(linkTask.name.replace("link", "pack"), Copy::class.java) {
                    group = "multiplatform"

                    from(resourcesGenerationDir)
                    into(framework.outputFile)
                }

            linkTask.finalizedBy(pack)
        }
    }

    companion object {
        const val BUNDLE_PROPERTY_NAME = "bundle"
    }
}