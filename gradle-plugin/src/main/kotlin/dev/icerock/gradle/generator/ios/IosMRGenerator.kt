/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.ios

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.generator.MRGenerator
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.kotlin.dsl.support.unzipTo
import org.jetbrains.kotlin.gradle.plugin.mpp.AbstractKotlinNativeCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile
import org.jetbrains.kotlin.konan.file.zipDirAs
import org.jetbrains.kotlin.library.impl.KotlinLibraryLayoutImpl
import java.io.File
import java.util.Properties

class IosMRGenerator(
    generatedDir: File,
    sourceSet: SourceSet,
    mrClassPackage: String,
    generators: List<Generator>,
    private val compilation: AbstractKotlinNativeCompilation,
    private val baseLocalizationRegion: String
) : MRGenerator(
    generatedDir = generatedDir,
    sourceSet = sourceSet,
    mrClassPackage = mrClassPackage,
    generators = generators
) {
    private val bundleClassName =
        ClassName("platform.Foundation", "NSBundle")
    private val bundleIdentifier = "$mrClassPackage.MR"

    override fun getMRClassModifiers(): Array<KModifier> = arrayOf(KModifier.ACTUAL)

    override fun processMRClass(mrClass: TypeSpec.Builder) {
        super.processMRClass(mrClass)

        mrClass.addProperty(
            PropertySpec.builder(
                BUNDLE_PROPERTY_NAME,
                bundleClassName,
                KModifier.PRIVATE
            )
                .delegate(CodeBlock.of("lazy { NSBundle.loadableBundle(\"$bundleIdentifier\") }"))
                .build()
        )
    }

    override fun getImports(): List<ClassName> = listOf(
        bundleClassName,
        ClassName(packageName = "dev.icerock.moko.resources", simpleName = "loadableBundle")
    )

    override fun apply(generationTask: Task, project: Project) {
        setupKLibResources(generationTask)
        setupFrameworkResources()
    }

    private fun setupKLibResources(generationTask: Task) {
        val compileTask: KotlinNativeCompile = compilation.compileKotlinTask
        compileTask.dependsOn(generationTask)

        compileTask.doLast {
            this as KotlinNativeCompile

            val klibFile = this.outputFile.get()
            val repackDir = File(klibFile.parent, klibFile.nameWithoutExtension)
            val resRepackDir = File(repackDir, "resources")

            unzipTo(zipFile = klibFile, outputDirectory = repackDir)

            val manifestFile = File(repackDir, "manifest")
            val manifest = Properties()
            manifest.load(manifestFile.inputStream())

            val uniqueName = manifest["unique_name"] as String

            val bundleDir = File(resRepackDir, "$uniqueName.bundle")
            bundleDir.mkdir()

            val bundleContentsDir = File(bundleDir, "Contents")
            bundleContentsDir.mkdir()

            val bundlePList = File(bundleContentsDir, "Info.plist")
            bundlePList.writeText(
                """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
	<key>CFBundleDevelopmentRegion</key>
	<string>$baseLocalizationRegion</string>
	<key>CFBundleIdentifier</key>
	<string>$bundleIdentifier</string>
	<key>CFBundleVersion</key>
	<string>1</string>
    <key>CFBundlePackageType</key>
	<string>BNDL</string>
</dict>
</plist>"""
            )

            val bundleResourcesDir = File(bundleContentsDir, "Resources")
            bundleResourcesDir.mkdir()

            resourcesGenerationDir.copyRecursively(bundleResourcesDir, overwrite = true)

            val repackKonan = org.jetbrains.kotlin.konan.file.File(repackDir.path)
            val klibKonan = org.jetbrains.kotlin.konan.file.File(klibFile.path)

            repackKonan.zipDirAs(klibKonan)

            repackDir.deleteRecursively()
        }
    }

    private fun setupFrameworkResources() {
        val kotlinNativeTarget = compilation.target as KotlinNativeTarget

        val frameworkBinaries: List<Framework> = kotlinNativeTarget.binaries
            .filterIsInstance<Framework>()
            .filter { it.compilation == compilation }

        frameworkBinaries.forEach { framework ->
            val linkTask = framework.linkTask

            linkTask.doLast {
                linkTask.libraries
                    .plus(linkTask.intermediateLibrary.get())
                    .filter { it.extension == "klib" }
                    .forEach {
                        project.logger.info("copy resources from $it")
                        val klibKonan = org.jetbrains.kotlin.konan.file.File(it.path)
                        val klib = KotlinLibraryLayoutImpl(klibKonan)
                        val layout = klib.extractingToTemp

                        File(layout.resourcesDir.path).copyRecursively(framework.outputFile, overwrite = true)
                    }
            }
        }
    }

    companion object {
        const val BUNDLE_PROPERTY_NAME = "bundle"
    }
}
