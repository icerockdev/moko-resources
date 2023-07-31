/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.jvm

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.generator.MRGenerator
import dev.icerock.gradle.utils.dependsOnProcessResources
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class JvmMRGenerator(
    generatedDir: Provider<Directory>,
    sourceSet: SourceSet,
    mrSettings: MRSettings,
    generators: List<Generator>
) : MRGenerator(
    generatedDir = generatedDir,
    sourceSet = sourceSet,
    mrSettings = mrSettings,
    generators = generators
) {
    private val flattenClassNameProvider: Provider<String> = mrSettings.packageName
        .map { it.replace(".", "") }
    override val resourcesGenerationDir: Provider<Directory> = outputDir.map {
        it.dir(flattenClassNameProvider.get()).dir("res")
    }

    override fun getMRClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun processMRClass(mrClass: TypeSpec.Builder) {
        super.processMRClass(mrClass)

        mrClass.addProperty(
            PropertySpec.builder(
                STRINGS_BUNDLE_PROPERTY_NAME,
                STRING,
                KModifier.PRIVATE
            ).initializer(
                CodeBlock.of(
                    "\"%L/%L\"",
                    LOCALIZATION_DIR,
                    "${flattenClassNameProvider.get()}_$STRINGS_BUNDLE_NAME"
                )
            ).build()
        )

        mrClass.addProperty(
            PropertySpec.builder(
                PLURALS_BUNDLE_PROPERTY_NAME,
                STRING,
                KModifier.PRIVATE
            ).initializer(
                CodeBlock.of(
                    "\"%L/%L\"",
                    LOCALIZATION_DIR,
                    "${flattenClassNameProvider.get()}_$PLURALS_BUNDLE_NAME"
                )
            ).build()
        )
    }

    override fun apply(generationTask: Task, project: Project) {
        project.tasks.withType<KotlinCompile>().configureEach {
            it.dependsOn(generationTask)
        }
        project.tasks.withType<Jar>().configureEach {
            it.exclude("MR/**")
            it.dependsOn(generationTask)
        }
        dependsOnProcessResources(
            project = project,
            sourceSet = sourceSet,
            task = generationTask,
            shouldExcludeGenerated = false
        )
    }

    companion object {
        const val STRINGS_BUNDLE_PROPERTY_NAME = "stringsBundle"
        const val PLURALS_BUNDLE_PROPERTY_NAME = "pluralsBundle"
        const val STRINGS_BUNDLE_NAME = "mokoBundle"
        const val PLURALS_BUNDLE_NAME = "mokoPluralsBundle"
        const val LOCALIZATION_DIR = "localization"
    }
}
